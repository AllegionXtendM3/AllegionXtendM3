/*
 ***************************************************************
 *                                                             *
 *                           NOTICE                            *
 *                                                             *
 *   THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS             *
 *   CONFIDENTIAL INFORMATION OF INFOR AND/OR ITS AFFILIATES   *
 *   OR SUBSIDIARIES AND SHALL NOT BE DISCLOSED WITHOUT PRIOR  *
 *   WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND       *
 *   ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH  *
 *   THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.            *
 *   ALL OTHER RIGHTS RESERVED.                                *
 *                                                             *
 *   (c) COPYRIGHT 2020 INFOR.  ALL RIGHTS RESERVED.           *
 *   THE WORD AND DESIGN MARKS SET FORTH HEREIN ARE            *
 *   TRADEMARKS AND/OR REGISTERED TRADEMARKS OF INFOR          *
 *   AND/OR ITS AFFILIATES AND SUBSIDIARIES. ALL RIGHTS        *
 *   RESERVED.  ALL OTHER TRADEMARKS LISTED HEREIN ARE         *
 *   THE PROPERTY OF THEIR RESPECTIVE OWNERS.                  *
 *                                                             *
 ***************************************************************
 */

 import groovy.lang.Closure
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import groovy.json.JsonSlurper;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.lang.NumberFormatException;

/*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *Star Track        20231113  WLAM        Star Track Integration - shipment List EXTREL records
 *Star Track        20240121  RMURRAY     Syntax, def to void, set dlix convert to long.
 */

public class Lst extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  
  //Input fields
  private String dlix;
  private List listEXTREL;
  
  private int XXCONO;
   
 /*
  * Get Delivery/Package extension table row
 */
  public Lst(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.program = program;
    
  }
  
  public void main() {
  	dlix = mi.inData.get("DLIX") == null ? '' : mi.inData.get("DLIX").trim();
		XXCONO = (Integer)program.LDAZD.CONO;
		
    // Validate input fields  	
		if (dlix.isEmpty()) {
      mi.error("Delivery Index must be entered");
      return;
    }

    try{
      dlixLong = parseLong(dlix);
    }catch(NumberFormatException e){
      mi.error("Number format exception DLIX")
      return;
    }
    
    // - validate dlix
    DBAction queryEXTREL = database.table("EXTREL").index("00").selection("EXCONO", "EXDLIX", "EXPANR", "EXWHLO").build();
    DBContainer EXTREL = queryEXTREL.getContainer();
    EXTREL.set("EXCONO", XXCONO);
    EXTREL.set("EXDLIX", dlix.toInteger());
    listEXTREL = new ArrayList();
    queryEXTREL.readAll(EXTREL, 2, lstEXTREL)
    if (listEXTREL.size() == 1) { 
       Map<String, String> record1 = (Map<String, String>) listEXTREL[0];
       mi.outData.put("CONO", record1.CONO);
       mi.outData.put("DLIX", record1.DLIX);
       mi.outData.put("PANR", record1.PANR);
       mi.outData.put("WHLO", record1.WHLO);
       mi.write();
    } else {
      if (listEXTREL.size() > 1) {
        for (int j=0;j<listEXTREL.size();j++) {
            Map<String, String> record2 = (Map<String, String>) listEXTREL[j];
            mi.outData.put("CONO", record2.CONO);
            mi.outData.put("DLIX", record2.DLIX);
            mi.outData.put("PANR", record2.PANR);
            mi.outData.put("WHLO", record2.WHLO);
            mi.write();
        }
      }
    }
  }
  /*
  * listEXTREL - Callback function to return EXTREL
  *
  */
  Closure<?> lstEXTREL = { DBContainer EXTREL ->
    String cono = EXTREL.get("EXCONO").toString().trim();
    String dlix = EXTREL.get("EXDLIX").toString().trim();
    String panr = EXTREL.get("EXPANR").toString().trim();
    String whlo = EXTREL.get("EXWHLO").toString().trim();
    Map<String,String> map = [CONO: cono, DLIX: dlix, PANR: panr, WHLO: whlo];
    listEXTREL.add(map);  
  }
    
  
}
