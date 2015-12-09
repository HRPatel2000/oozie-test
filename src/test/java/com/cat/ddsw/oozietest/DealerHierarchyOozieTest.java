package com.cat.ddsw.oozietest;

import com.oozierunner.core.FileManager;
import com.oozierunner.core.OozieRunner;
import org.apache.oozie.client.OozieClientException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by hpatel on 8/31/2015.
 */
public class DealerHierarchyOozieTest {

    /**
     *
     * ####IMPORTANT####
     * #Relative Path to the DOMAIN Specific Job Properties is required as below
     */
    private static final String jobPropertiesFile = "dealer_hierarchy/job.properties";

    /**
     *
     * ####IMPORTANT####
     * #Relative Path to the Expected and Actual Data Path should be Drequired as below
     *  1. Expected - Expected Test Results in a File must be provided against "Each Query" under the Test Case (One Record Per Line)
     *  2. Actual - OozieRunner Framework will execute the Query under the Test Case and save result data into the file under this path (One Record Per Line)
     *
     * ####IMPORTANT####
     * #New Line Characters under the Expected file will break the Content Comparision.
     * # So make sure the Expected Result files are free of New Line Character at the end of the "Last Record"
     *
     */
    private static final String actualDataPath = "src/test/resources/dealer_hierarchy/actual";
    private static final String expectedDataPath = "src/test/resources/dealer_hierarchy/expected";

    private static OozieRunner oozieRunner = null;

    /**
     *
     * ####IMPORTANT####
     * #Naming convention must be maintained for DOMAIN Specific Properties below
     * #Provide Test Data Source and Target Files for each Domain with Keys as "<DOMAIN_NAME>.test_data_source_file" AND "<DOMAIN_NAME>.test_data_target_file"
     * #Also, assumption is that the Test Data Target File is monitored by individual Domain Workflows for processing
     *
     */
    private static final String sourceDataFile = "/projects/ddsw/qa/data/cicd/test_data/dealer_hierarchy/ZZZZ_Dealer_Hierarchy.xml_88888888_0001_1429071717.complete.xml";
    private static final String targetDataFile = "/projects/ddsw/qa/data/raw/nas/dealer_hierarchy/ZZZZ_Dealer_Hierarchy.xml_88888888_0001_1429071717.complete";

    /**
     * Encoding utf-8 is required for File Content comparison to work in Windows.
     * OozieRunner.executeQuery, FileManager.getFileContent and FileManager.contentEquals methods requires this information.
     */
    String encoding = "utf-8";

    /**
     *
     * ****IMPORTANT****
     * Every Test case that needs to submit Oozie workflow Job needs this @BeforeClass annotation with,
     *  1. Create Instance of OozieRunner with supplying Domain specific Job Properties file
     *  2. Call CopyHDFSData with Source and Target Data Files on HDFS (If Test data is expected in a specific HDFS location prior to running the Workflow Job
     *  3. Submit the actual Workflow Job
     */
    @BeforeClass
    public static void initialize() {

        System.out.println("DealerHierarchyOozieTest@BeforeClass...");

        try {
            oozieRunner = new OozieRunner(jobPropertiesFile);

            oozieRunner.copyHDFSData(sourceDataFile, targetDataFile);
            oozieRunner.submitOozieJob();

        } catch (OozieClientException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @Test
    public void testDealerHierarchyCoreTableCount() {

        System.out.println("DealerHierarchyOozieTest@testDealerHierarchyCoreTableCount...");
        String expectedDataFile = expectedDataPath + "/dealer_hierarchy_current_table_count.csv";
        String actualDataFile = actualDataPath + "/dealer_hierarchy_current_table_count.csv";

        String query = "select count(*) from ddsw_qa.dealer_hierarchy_current where dealer_code ='ZZZZ'";

        try {
            oozieRunner.executeHiveQuery(query, actualDataFile, "\t", encoding);

            Assert.assertTrue(FileManager.contentEquals(actualDataFile, expectedDataFile, encoding));
        } catch (OozieClientException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDealerHierarchyCoreTableContent() {

        System.out.println("DealerHierarchyOozieTest@testDealerHierarchyCoreTableContent...");
        String expectedDataFile = expectedDataPath + "/dealer_hierarchy_current_table_content.csv";
        String actualDataFile = actualDataPath + "/dealer_hierarchy_current_table_content.csv";

        /**
         *
         *####IMPORTANT####
         * Timestamp fields would break the Content Comparision if they represents current time or change dynamically.
         * So, it may be okay to just skip the Timestamp fields from Select Queries if possible!!!
         *
         */

        String query = "select " +
                "file_name, gen_id, dms_system, dms_version, transmit_by, record_status, dealer_updt_ind, store_number, " +
                "part_type, store_name, inventory_store_number, cat_emrg_dealer_code, cat_stock_dealer_code, " +
                "stock_replen_store_number, stock_replen_lead_time, lst_updt_by_id, exp_stock_plan_lead_time, dealer_code" +
                " from ddsw_qa.dealer_hierarchy_current where dealer_code ='ZZZZ'";

        try {
            oozieRunner.executeHiveQuery(query, actualDataFile, "\t", encoding);

            Assert.assertTrue(FileManager.contentEquals(actualDataFile, expectedDataFile, encoding));
        } catch (OozieClientException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * ****IMPORTANT****
     * Every Test case that needs to perform Cleanup of Test data etc at the end of the Test must provide this @AfterClass annotation with,
     *  1. Create Instance of OozieRunner with supplying Domain specific Job Properties file
     *  2. Call CopyHDFSData with Source and Target Data Files on HDFS (If Test data is expected in a specific HDFS location prior to running the Workflow Job
     *  3. Submit the actual Workflow Job
     */
    @AfterClass
    public static void cleanup() {
        System.out.println("DealerHierarchyOozieTest@AfterClass...");

        try {

            //Delete Actual Data Path where Test Result Files were stored
            oozieRunner.deleteDirectoryContent(actualDataPath);

            //Delete Fake Dealer Partition in Each Table the Workflow has created Fake Data in
            oozieRunner.dropPartition("dealer_hierarchy_core", "dealer_code='ZZZZ'");

            oozieRunner.dropPartition("dealer_hierarchy_current", "dealer_code='ZZZZ'");

        } catch (OozieClientException e) {
            e.printStackTrace();
        }
    }
}
