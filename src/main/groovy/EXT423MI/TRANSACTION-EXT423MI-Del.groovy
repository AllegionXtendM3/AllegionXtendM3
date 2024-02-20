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
 import groovy.json.JsonSlurper;
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import java.time.ZoneId;
 import java.lang.NumberFormatException;

/*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *Star Track        20231113  WLAM        Star Track Integration - shipment delete EXTREL records
 *Star Track        20240121  RMURRAY     Syntax, def to void, set dlix convert to long.
 */

/*
* - Delete the record to EXTREL
*/

public class Del extends ExtendM3Transaction {

  private final MIAPI mi;
  private final DatabaseAPI database;
  private final ProgramAPI program;
  
  //Input fields
  private String dlix;
  private Long dlixLong;

  private int XXCONO;
 
 /*
  * Delete Delivery extension table row
 */
  public Del(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi;
    this.database = database;
    this.program = program;
  }

  public void main() {
    dlix = mi.inData.get("DLIX") == null ? '' : mi.inData.get("DLIX").trim();
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
    XXCONO = (Integer)program.LDAZD.CONO;
    deleteEXTREL(dlix);
  }
  	
  /*
  * Delete extension table EXTREL
  *
  */
  private void deleteEXTREL(String dlix) {
    DBAction actionEXTREL = database.table("EXTREL").build();
    DBContainer EXTREL = actionEXTREL.getContainer();
    EXTREL.set("EXCONO", XXCONO);
    EXTREL.set("EXDLIX", dlixLong);
    actionEXTREL.readAllLock(EXTREL, 2, delEXTREL);
  }
  /*
   * deleteEXTREL - Callback function
   *
  */
  Closure<?> delEXTREL = { LockedResult EXTREL ->
    EXTREL.delete();
  }  

}
