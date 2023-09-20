import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
 
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
 
/**
 * Program Name
 *  MQClearQueue01
 *
 * Description
 *  This java class issues a PCF "Clear Q" command for a queue to delete all messages 
 *  in the queue of a remote queue manager.
 *    
 *  Note: The PCF "Clear Q" command will fail if an application has the queue open.
 *
 * Sample Command Line Parameters
 *  -m MQA1 -h 127.0.0.1 -p 1414 -c TEST.CHL -q TEST.Q1 -u UserID -x Password
 *
 * @author Roger Lacroix
 */
public class MQClearQueue01
{
   private static final SimpleDateFormat  LOGGER_TIMESTAMP = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
 
   private Hashtable<String,String> params;
   private Hashtable<String,Object> mqht;
 
   public MQClearQueue01()
   {
      super();
      params = new Hashtable<String,String>();
      mqht = new Hashtable<String,Object>();
   }
 
   /**
    * Make sure the required parameters are present.
    * @return true/false
    */
   private boolean allParamsPresent()
   {
      boolean b = params.containsKey("-h") && params.containsKey("-p") &&
                  params.containsKey("-c") && params.containsKey("-m") &&
                  params.containsKey("-q") &&
                  params.containsKey("-u") && params.containsKey("-x");
      if (b)
      {
         try
         {
            Integer.parseInt((String) params.get("-p"));
         }
         catch (NumberFormatException e)
         {
            b = false;
         }
      }
 
      return b;
   }
 
   /**
    * Extract the command-line parameters and initialize the MQ HashTable.
    * @param args
    * @throws IllegalArgumentException
    */
   private void init(String[] args) throws IllegalArgumentException
   {
      int port = 1414;
      if (args.length > 0 && (args.length % 2) == 0)
      {
         for (int i = 0; i < args.length; i += 2)
         {
            params.put(args[i], args[i + 1]);
         }
      }
      else
      {
         throw new IllegalArgumentException();
      }
 
      if (allParamsPresent())
      {
         try
         {
            port = Integer.parseInt((String) params.get("-p"));
         }
         catch (NumberFormatException e)
         {
            port = 1414;
         }
 
         mqht.put(CMQC.CHANNEL_PROPERTY, params.get("-c"));
         mqht.put(CMQC.HOST_NAME_PROPERTY, params.get("-h"));
         mqht.put(CMQC.PORT_PROPERTY, new Integer(port));
         mqht.put(CMQC.USER_ID_PROPERTY, params.get("-u"));
         mqht.put(CMQC.PASSWORD_PROPERTY, params.get("-x"));
 
         // I don't want to see MQ exceptions at the console.
         MQException.log = null;
      }
      else
      {
         throw new IllegalArgumentException();
      }
   }
 
   /**
    * Handle connecting to the queue manager, issuing PCF command then
    * looping through PCF response messages and disconnecting from
    * the queue manager.
    */
   private void doPCF()
   {
      MQQueueManager  qMgr   = null;
      PCFMessageAgent agent  = null;
      PCFMessage   request   = null;
      PCFMessage[] responses = null;
      String qMgrName  = (String) params.get("-m");
      String queueName = (String) params.get("-q");
 
      try
      {
         qMgr = new MQQueueManager(qMgrName, mqht);
         MQClearQueue01.logger("successfully connected to "+ qMgrName);
 
         agent = new PCFMessageAgent(qMgr);
         MQClearQueue01.logger("successfully created agent");
 
         // https://www.ibm.com/docs/en/ibm-mq/latest?topic=formats-clear-queue
         request = new PCFMessage(CMQCFC.MQCMD_CLEAR_Q);
 
         request.addParameter(CMQC.MQCA_Q_NAME, queueName);
 
         responses = agent.send(request);
 
         MQClearQueue01.logger("responses.length="+responses.length);
 
         for (int i = 0; i < responses.length; i++)
         {
            if ((responses[i]).getCompCode() == CMQC.MQCC_OK)
               MQClearQueue01.logger("Successfully cleared queue '"+queueName+"' of messages.");
            else
               MQClearQueue01.logger("Error: Failed to clear queue '"+queueName+"' of messages.");
         }
      }
      catch (MQException e)
      {
         MQClearQueue01.logger("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
      }
      catch (IOException e)
      {
         MQClearQueue01.logger("IOException:" +e.getLocalizedMessage());
      }
      catch (MQDataException e)
      {
         if ( (e.completionCode == CMQC.MQCC_FAILED) && (e.reasonCode == CMQCFC.MQRCCF_OBJECT_OPEN) )
            MQClearQueue01.logger("Error: Failed to clear queue '"+queueName+"' of messages. An application has the queue open.");
         else
            MQClearQueue01.logger("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
      }
      finally
      {
         try
         {
            if (agent != null)
            {
               agent.disconnect();
               MQClearQueue01.logger("disconnected from agent");
            }
         }
         catch (MQDataException e)
         {
            MQClearQueue01.logger("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
         }
 
         try
         {
            if (qMgr != null)
            {
               qMgr.disconnect();
               MQClearQueue01.logger("disconnected from "+ qMgrName);
            }
         }
         catch (MQException e)
         {
            MQClearQueue01.logger("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
         }
      }
   }
 
   /**
    * A simple logger method
    * @param data
    */
   public static void logger(String data)
   {
      String className = Thread.currentThread().getStackTrace()[2].getClassName();
 
      // Remove the package info.
      if ( (className != null) && (className.lastIndexOf('.') != -1) )
         className = className.substring(className.lastIndexOf('.')+1);
 
      System.out.println(LOGGER_TIMESTAMP.format(new Date())+" "+className+": "+Thread.currentThread().getStackTrace()[2].getMethodName()+": "+data);
   }
 
   public static void main(String[] args)
   {
      MQClearQueue01 mqcq = new MQClearQueue01();
 
      try
      {
         mqcq.init(args);
         mqcq.doPCF();
      }
      catch (IllegalArgumentException e)
      {
         MQClearQueue01.logger("Usage: java MQClearQueue01 -m QueueManagerName -h host -p port -c channel -q QueueName -u UserID -x Password");
         System.exit(1);
      }
 
      System.exit(0);
   }
}
