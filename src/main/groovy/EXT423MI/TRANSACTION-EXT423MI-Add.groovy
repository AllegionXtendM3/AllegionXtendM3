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
 import java.time.ZoneId;
 import groovy.json.JsonSlurper;
 import java.lang.NumberFormatException;


/*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *Star Track        20231113  WLAM        Star Track Integration - shipment Add EXTREL records
 *Star Track        20240121  RMURRAY     Syntax, def to void, set dlix convert to long.
 */

/*
* - Write the record to EXTREL
*/

public class Add extends ExtendM3Transaction {

  private final MIAPI mi;
  private final DatabaseAPI database;
  private final ProgramAPI program;
  
  //Input fields
  private String dlix;
  private Long dlixLong;
  private String panr;
  private String head;
  private Integer dipa;
  private String whlo;
  private int XXCONO;
 
 /*
  * Add Delivery extension table row
 */
  public Add(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi;
    this.database = database;
    this.program = program;
  }

  public void main() {
    dlix = mi.inData.get("DLIX") == null ? '' : mi.inData.get("DLIX").trim();
    panr = mi.inData.get("PANR") == null ? '' : mi.inData.get("PANR").trim();
    whlo = mi.inData.get("WHLO") == null ? '' : mi.inData.get("WHLO").trim();
    head = mi.inData.get("HEAD") == null ? '' : mi.inData.get("HEAD").trim();
    dipa = 0;
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
    if (whlo.isEmpty()) {
      mi.error("Warehouse must be entered");
      return;
    }
    if (panr.isEmpty()) {
      mi.error("Package Number must be entered");
      return;
    }
    if (head.isEmpty()) {
      mi.error("HEAD must be either Y or N");
      return;
    }
    // - validate dlix
    DBAction queryMHDISH = database.table("MHDISH").index("00").selection("OQDLIX").build();
    DBContainer MHDISH = queryMHDISH.getContainer();
    MHDISH.set("OQCONO", XXCONO);
    MHDISH.set("OQINOU", 1);
    MHDISH.set("OQDLIX", dlixLong);
    if (!queryMHDISH.read(MHDISH)) {
      mi.error("Delivery Index is invalid." + XXCONO + " DLIX= " + dlix);
      return;
    }  
    // - validate panr
    DBAction queryMPTRNS = database.table("MPTRNS").index("00").selection("ORCONO", "ORDIPA", "ORDLIX", "ORPANR").build();
    DBContainer MPTRNS = queryMPTRNS.getContainer();
    MPTRNS.set("ORCONO", XXCONO);
    MPTRNS.set("ORDIPA", dipa);
    MPTRNS.set("ORWHLO", whlo);
    MPTRNS.set("ORDLIX", dlixLong);
    MPTRNS.set("ORPANR", panr);
    if (!queryMPTRNS.read(MPTRNS)) {
      mi.error("Package number is invalid" + XXCONO + " DLIX= " + dlix + " PANR=" + panr);
      return;
    }    
    // - validate whlo
    DBAction queryMITWHL = database.table("MITWHL").index("00").selection("MWCONO", "MWWHLO").build();
    DBContainer MITWHL = queryMITWHL.getContainer();
    MITWHL.set("MWCONO", XXCONO);
    MITWHL.set("MWWHLO", whlo);
    if (!queryMITWHL.read(MITWHL)) {
      mi.error("Warehouse is invalid." + XXCONO + " WHLO= " + whlo);
      return;
    }  
    if (head == 'Y' || head == 'N') {
      
    } else {
      mi.error("HEAD must be either Y or N");
      return;
    }
    writeEXTREL(dlix, panr, whlo, head);
  }
  /*
  * Write extension table EXTREL
  *
  */
  private void writeEXTREL(String dlix, String panr, String whlo, String head) {
    //Current date and time
    int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
    DBAction actionEXTREL = database.table("EXTREL").build();
    DBContainer EXTREL = actionEXTREL.getContainer();
    EXTREL.set("EXCONO", XXCONO);
    EXTREL.set("EXDLIX", dlixLong);
    EXTREL.set("EXPANR", panr);
    EXTREL.set("EXWHLO", whlo);
    EXTREL.set("EXHEAD", head);
    EXTREL.set("EXRGDT", currentDate);
    EXTREL.set("EXRGTM", currentTime);
    EXTREL.set("EXLMDT", currentDate);
    EXTREL.set("EXCHNO", 0);
    EXTREL.set("EXCHID", program.getUser());
    actionEXTREL.insert(EXTREL, recordExists);
  }
  /*
   * recordExists - return record already exists error message to the MI
   *
  */
  Closure recordExists = {
    mi.error("Record already exists");
  }  	

}
