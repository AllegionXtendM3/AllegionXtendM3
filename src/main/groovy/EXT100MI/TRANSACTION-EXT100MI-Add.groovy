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

/*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *Star Track        20231113  WLAM        Star Track Integration - shipment Add EXTCNN records
 *Star Track        20240121  RMURRAY     Syntax, def to void, set dlix convert to long. removed unused declarations
 */

/*
* - Write the record to EXTCNN
*/

public class Add extends ExtendM3Transaction {

  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  
  //Input fields
  private String conn;

  private int XXCONO;
 
 /*
  * Add Delivery extension table row
 */
  public Add(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program) {
    this.mi = mi;
    this.database = database;
    this.miCaller = miCaller;
    this.program = program; 
  }

  public void main() {
    conn = mi.inData.get("CONN") == null ? '' : mi.inData.get("CONN").trim();
    XXCONO = (Integer)program.LDAZD.CONO;
		
    // Validate input fields  	
    if (conn.isEmpty()) {
      mi.error("Shipment must be entered");
      return;
    }

    // - validate conn
    DBAction queryDCONSI = database.table("DCONSI").index("00").selection("DACONN").build();
    DBContainer DCONSI = queryDCONSI.getContainer();
    DCONSI.set("DACONO", XXCONO);
    DCONSI.set("DACONN", conn.toInteger());
    if (!queryDCONSI.read(DCONSI)) {
      mi.error("Shipment is invalid." + XXCONO + " CONN= " + conn);
      return;
    }  
  	writeEXTCNN(conn);
  }
  	
  /*
  * Write extension table EXTCNN
  *
  */
  private void writeEXTCNN(String conn) {
    //Current date and time
    int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	
    DBAction actionEXTCNN = database.table("EXTCNN").build();
    DBContainer EXTCNN = actionEXTCNN.getContainer();
    EXTCNN.set("EXCONO", XXCONO);
    EXTCNN.set("EXCONN", conn.toInteger());
    EXTCNN.set("EXRGDT", currentDate);
    EXTCNN.set("EXRGTM", currentTime);
    EXTCNN.set("EXLMDT", currentDate);
    EXTCNN.set("EXCHNO", 0);
    EXTCNN.set("EXCHID", program.getUser());
    actionEXTCNN.insert(EXTCNN, recordExists);
  }
  /*
   * recordExists - return record already exists error message to the MI
   *
  */
  Closure recordExists = {
    mi.error("Record already exists");
  }  	

}
