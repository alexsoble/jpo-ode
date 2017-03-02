package us.dot.its.jpo.ode.plugin.j2735;

import us.dot.its.jpo.ode.plugin.asn1.Asn1Object;

public class J2735RegionOffsets extends Asn1Object {

   private static final long serialVersionUID = 2694144791459199713L;

   protected Long xOffsetCm;
   protected Long yOffsetCm;
   protected Long zOffsetCm;

   public J2735RegionOffsets() {
      super();
   }

   public J2735RegionOffsets(Long xOffsetCm, Long yOffsetCm, Long zOffsetCm) {
      this.xOffsetCm = xOffsetCm;
      this.yOffsetCm = yOffsetCm;
      this.zOffsetCm = zOffsetCm;
   }

   public Long getxOffsetCm() {
   	return xOffsetCm;
   }

   public J2735RegionOffsets setxOffsetCm(Long xOffsetCm) {
   	this.xOffsetCm = xOffsetCm;
   	return this;
   }

   public Long getyOffsetCm() {
   	return yOffsetCm;
   }

   public J2735RegionOffsets setyOffsetCm(Long yOffsetCm) {
   	this.yOffsetCm = yOffsetCm;
   	return this;
   }

   public Long getzOffsetCm() {
   	return zOffsetCm;
   }

   public J2735RegionOffsets setzOffsetCm(Long zOffsetCm) {
   	this.zOffsetCm = zOffsetCm;
   	return this;
   }

}