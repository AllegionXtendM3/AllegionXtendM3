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
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;


/*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *Star Track        20231113  WLAM        Star Track Integration - shipment delete EXTCNN records
 *
 */

/*
* - Delete the record to EXTREL
*/

public class Del extends ExtendM3Transaction {

  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  private String conn;

  private int XXCONO;
 
 /*
  * Delete Delivery extension table row
 */
  public Del(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
	  
  }

  public void main() {
  	conn = mi.inData.get("CONN") == null ? '' : mi.inData.get("CONN").trim();
		XXCONO = (Integer)program.LDAZD.CONO;
		
    // Validate input fields  	
		if (conn.isEmpty()) {
      mi.error("Shipment must be entered");
      return;
    }
    
  	deleteEXTCNN(conn);
  }
  	
  /*
  * Delete extension table EXTCNN
  *
  */
  def deleteEXTCNN(String conn) {

	  DBAction actionEXTCNN = database.table("EXTCNN").build();
  	DBContainer EXTCNN = actionEXTCNN.getContainer();
  	EXTCNN.set("EXCONO", XXCONO);
  	EXTCNN.set("EXCONN", conn.toInteger());
  	actionEXTCNN.readLock(EXTCNN, delEXTCNN);
	}
  /*
   * deleteEXTREL - Callback function
   *
  */
  Closure<?> delEXTCNN = { LockedResult EXTCNN ->
    EXTCNN.delete();
  }  

}
