package us.dot.its.jpo.ode.services.asn1;

import java.io.IOException;
import java.text.ParseException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.ScopedPDU;
import org.snmp4j.event.ResponseEvent;

import us.dot.its.jpo.ode.OdeProperties;
import us.dot.its.jpo.ode.context.AppContext;
import us.dot.its.jpo.ode.dds.DdsClient.DdsClientException;
import us.dot.its.jpo.ode.dds.DdsDepositor;
import us.dot.its.jpo.ode.dds.DdsRequestManager.DdsRequestManagerException;
import us.dot.its.jpo.ode.dds.DdsStatusMessage;
import us.dot.its.jpo.ode.model.OdeAsn1Data;
import us.dot.its.jpo.ode.model.TravelerInputData;
import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;
import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.snmp.SnmpSession;
import us.dot.its.jpo.ode.traveler.TimController.TimControllerException;
import us.dot.its.jpo.ode.traveler.TimPduCreator;
import us.dot.its.jpo.ode.traveler.TimPduCreator.TimPduCreatorException;
import us.dot.its.jpo.ode.util.JsonUtils;
import us.dot.its.jpo.ode.util.XmlUtils;
import us.dot.its.jpo.ode.wrapper.AbstractSubscriberProcessor;
import us.dot.its.jpo.ode.wrapper.WebSocketEndpoint.WebSocketException;

public class Asn1EncodedDataRouter extends AbstractSubscriberProcessor<String, String> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private OdeProperties odeProperties;
    private DdsDepositor<DdsStatusMessage> depositor;
    
    public Asn1EncodedDataRouter(OdeProperties odeProps) {
      super();
      this.odeProperties = odeProps;

      try {
         depositor = new DdsDepositor<>(this.odeProperties);
      } catch (Exception e) {
         logger.error("Error starting SDW depositor", e);
      }

    }

    @Override
    public Object process(String consumedData) {
        try {
           JSONObject consumedObj = XmlUtils.toJSONObject(consumedData)
                 .getJSONObject(OdeAsn1Data.class.getSimpleName());

           // Convert JSON to POJO
           TravelerInputData travelerinputData = buildTravelerInputData(consumedObj);
           
           processEncodedTim(travelerinputData, consumedObj);
           
        } catch (Exception e) {
           logger.error("Error in processing received message from ASN.1 Encoder module: " + consumedData, e);
        }
        return null;
    }

    public TravelerInputData buildTravelerInputData(JSONObject consumedObj) {
       String request = consumedObj
             .getJSONObject(AppContext.METADATA_STRING)
             .getJSONObject("request").toString();
       
       // Convert JSON to POJO
       TravelerInputData travelerinputData = null;
       try {
          logger.debug("JSON: {}", request);
          travelerinputData = (TravelerInputData) JsonUtils.fromJson(request, TravelerInputData.class);

       } catch (Exception e) {
          String errMsg = "Malformed JSON.";
          logger.error(errMsg, e);
       }

       return travelerinputData;
    }

    public void processEncodedTim(TravelerInputData travelerInfo, JSONObject consumedObj) throws TimControllerException {
       // Send TIMs and record results
       //HashMap<String, String> responseList = new HashMap<>();

       JSONObject dataObj = consumedObj
             .getJSONObject(AppContext.PAYLOAD_STRING)
             .getJSONObject(AppContext.DATA_STRING);
       
       JSONObject asdObj = dataObj.getJSONObject("AdvisorySituationData");
       if (null != asdObj) {
          String asdBytes = asdObj.getString("bytes");

          // Deposit to DDS
          try {
             depositToDDS(travelerInfo, asdBytes);
             logger.info("DDS deposit successful.");
          } catch (Exception e) {
             logger.error("Error on DDS deposit.", e);
          }
       }
       
       // Deposit to RSUs
       JSONObject mfObj = dataObj.getJSONObject("MessageFrame");
       if (null != mfObj) {
          String timBytes = mfObj.getString("bytes");
          for (RSU curRsu : travelerInfo.getRsus()) {
             
             logger.info("Depositing TIM via SNMP to RSU {}", curRsu.getRsuTarget());

             ResponseEvent rsuResponse = null;
             //String httpResponseStatus = null;

             try {
                rsuResponse = createAndSend(travelerInfo.getSnmp(), curRsu, 
                   travelerInfo.getOde().getIndex(), timBytes);

                if (null == rsuResponse || null == rsuResponse.getResponse()) {
                   logger.error("RSU SNMP deposit to {} failed, timeout.", curRsu.getRsuTarget());
                } else if (rsuResponse.getResponse().getErrorStatus() == 0) {
                   logger.info("RSU SNMP deposit to {} successful.", curRsu.getRsuTarget());
                } else if (rsuResponse.getResponse().getErrorStatus() == 5) {
                   logger.error("RSU SNMP deposit to {} failed, message already exists at index {}.", curRsu.getRsuTarget(), travelerInfo.getOde().getIndex());
                } else {
                   logger.error("RSU SNMP deposit to {} failed, error code {}, error: {}", curRsu.getRsuTarget(), rsuResponse.getResponse().getErrorStatus(), rsuResponse.getResponse().getErrorStatusText());
                }

             } catch (Exception e) {
                logger.error("Exception caught in TIM RSU SNMP deposit loop.", e);
             }
          }
       }
    }

    /**
     * Create an SNMP session given the values in
     * 
     * @param tim
     *           - The TIM parameters (payload, channel, mode, etc)
     * @param props
     *           - The SNMP properties (ip, username, password, etc)
     * @return ResponseEvent
     * @throws TimPduCreatorException
     * @throws IOException
     */
    public static ResponseEvent createAndSend(SNMP snmp, RSU rsu, int index, String payload)
          throws IOException, TimPduCreatorException {

       SnmpSession session = new SnmpSession(rsu);

       // Send the PDU
       ResponseEvent response = null;
       ScopedPDU pdu = TimPduCreator.createPDU(snmp, payload, index);
       response = session.set(pdu, session.getSnmp(), session.getTarget(), false);
       return response;
    }

    private void depositToDDS(TravelerInputData travelerinputData, String asdBytes)
          throws ParseException, DdsRequestManagerException, DdsClientException, WebSocketException {
       // Step 4 - Step Deposit TIM to SDW if sdw element exists
       if (travelerinputData.getSdw() != null) {
          depositor.deposit(asdBytes);
       }
    }

}