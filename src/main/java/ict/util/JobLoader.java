package ict.util;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ict.scheduler.Job;
import ict.scheduler.Resource;
import ict.scheduler.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * load job info from xml
 * Created by Zongzan on 2016/11/18.
 */

public class JobLoader {

    private static final Log LOG = LogFactory.getLog(JobLoader.class);

    private String xmlPath = "";


    public JobLoader(String xmlUrl) {
        this.xmlPath = xmlUrl;
    }

    public List<Job> getJobFromXML() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;

        Document document = null;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new FileInputStream(xmlPath)));
            System.out.println("read xml file.");
        } catch (ParserConfigurationException e) {
            LOG.error("Load job from XML error.");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            LOG.error("Load job from XML error.");
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("Load job from XML error.");
            e.printStackTrace();
        } catch (SAXException e) {
            LOG.error("Load job from XML error.");
            e.printStackTrace();
        }
        return getModel(document);
    }

    private List<Job> getModel(Document doc) {

        List<Job> jobs = new ArrayList<Job>();
        Element rootElement = doc.getDocumentElement();
        NodeList jobList = rootElement.getElementsByTagName("job");
        for(int q = 0; q < jobList.getLength();q++){
            Node jobnode = jobList.item(q);
            NodeList nodeList = jobnode.getChildNodes();

            int priority = 0;
            Job job = new Job();

            int len = nodeList.getLength();
            List<Task> taskList = new ArrayList<Task>();
            for(int i = 0; i < len; i++){
                Node node = nodeList.item(i);
                if(node instanceof Element){
                    // job
                    String content = node.getLastChild().getTextContent().trim();
                    switch (node.getNodeName()) {
                        case "jobId":
                            job.setJobId(content);                  break;
                        case "jobName":
                            job.setJobName(content);                break;
                        case "description":
                            job.setDescription(content);            break;
                        case "priority":
                            priority = Integer.parseInt(content);   break;
                        case "starttime":
                            job.setStarttime(content);              break;
                        case "tasks": {
                            // getChild
                            // get Element
                            NodeList tasks = ((Element) node).getElementsByTagName("task");
                            // System.out.println(tasks.getLength());

                            for (int j = 0; j < tasks.getLength(); j++){
                                Task t = new Task();
                                int cores = 0;
                                int ram = 0;
                                double csps = 0;
                                Node task = tasks.item(j);
                                NodeList taskItems = task.getChildNodes();
                                for(int k = 0; k < taskItems.getLength(); k++){
                                    // task
                                    Node taskItem = taskItems.item(k);
                                    if(taskItem instanceof Element){
                                        //System.out.println("nodename:"+taskItem.getNodeName());
                                        String taskCon = taskItem.getFirstChild().getNodeValue().trim();
                                        //System.out.println("value:"+taskCon);
                                        switch (taskItem.getNodeName()){
                                            case "taskId":
                                                t.setTaskId(taskCon);                           break;
                                            case "execSequence":
                                                t.setExecSequence(Integer.parseInt(taskCon));   break;
                                            case "jarPath":
                                                t.setJarPath(taskCon);                          break;
                                            case "resourceAlloc":{
                                                // resource
                                                NodeList resItems = taskItem.getChildNodes();
                                                for(int m = 0; m < resItems.getLength(); m++){
                                                    Node resItem = resItems.item(m);
                                                    if(resItem instanceof Element){
                                                        String resAlloc = resItem.getFirstChild().getNodeValue().trim();
                                                        switch (resItem.getNodeName()) {
                                                            case "cores":
                                                                cores = Integer.parseInt(resAlloc);   break;
                                                            case "ram":
                                                                ram = Integer.parseInt(resAlloc);     break;
                                                        }
                                                    }
                                                }
                                            };  break;
                                            case "resourceConsume":{
                                                NodeList resItems = taskItem.getChildNodes();
                                                for(int n = 0; n < resItems.getLength(); n++) {
                                                    Node resItem = resItems.item(n);
                                                    if(resItem instanceof Element){
                                                        String resCon = resItem.getFirstChild().getNodeValue().trim();
                                                        switch (resItem.getNodeName()) {
                                                            case "csps":
                                                                csps = Double.parseDouble(resCon);     break;
                                                        }
                                                    }
                                                }

                                            };  break;
                                        }
                                    }
                                }

                                Resource resource = new Resource(cores, ram);
                                resource.setScps(csps);
                                t.setResourceRequests(resource);
                                t.setJobId(job.getJobId());
                                taskList.add(t);
                                //System.out.println(t);
                            }
                        }
                    }
                }
            }
            job.setTasks(taskList);
            for(Task task : taskList){
                task.setPriority(priority);
            }
            jobs.add(job);
            //System.out.println(job);
            LOG.info("Load job from xml. Job=" + job);
        }
        System.out.println("Job's number=" + jobs.size());
        return jobs;
    }

     public static void main(String[] args) {
         List<Job> jobs = new JobLoader("D://job.xml").getJobFromXML();
         int[] count = {0,0,0,0,0,0,0};
         for(Job job : jobs) {
            Task t = job.getTasks().get(0);
             double scps = t.getResourceRequests().getScps();
             if(scps <= 50)
                 count[0]++;
             else if(scps <= 100)
                 count[1]++;
             else if(scps <= 150)
                 count[2]++;
             else if(scps <= 200)
                 count[3]++;
             else if(scps <= 250)
                 count[4]++;
             else if(scps <= 300)
                 count[5]++;
             else count[6]++;
         }
         for(int i = 0 ;i < 7; i++){
             System.out.println(count[i]);
         }

    }


}
