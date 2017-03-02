package us.dot.its.jpo.ode.plugin.j2735;

import us.dot.its.jpo.ode.plugin.asn1.Asn1Object;

public class J2735RegionalContent extends Asn1Object {
   private static final long serialVersionUID = 1L;

   public enum RegionId {
      noRegion(J2735RegionalExtension.class), // Use default supplied stubs
      addGrpA(J2735RegionalExtension.class), // RegionId ::= 1 -- USA
      addGrpB(J2735RegionalExtension.class), // RegionId ::= 2 -- Japan
      addGrpC(J2735RegionalExtension.class) // RegionId ::= 3 -- EU
      ;

      Class<?> type;

      private RegionId(Class<?> type) {
         this.type = type;
      }

   }

   private Integer id;
   private String hexValue;

   public Integer getId() {
      return id;
   }

   public J2735RegionalContent setId(Integer id) {
      this.id = id;
      return this;
   }

   public String getHexValue() {
      return hexValue;
   }

   public J2735RegionalContent setHexValue(String hexValue) {
      this.hexValue = hexValue;
      return this;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((hexValue == null) ? 0 : hexValue.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      J2735RegionalContent other = (J2735RegionalContent) obj;
      if (hexValue == null) {
         if (other.hexValue != null)
            return false;
      } else if (!hexValue.equals(other.hexValue))
         return false;
      if (id == null) {
         if (other.id != null)
            return false;
      } else if (!id.equals(other.id))
         return false;
      return true;
   }
}
