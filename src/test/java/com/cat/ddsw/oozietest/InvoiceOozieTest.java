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
 * Created by bajwag on 9/16/2015.
 */
public class InvoiceOozieTest {

    /**
     *
     * ####IMPORTANT####
     * #Relative Path to the DOMAIN Specific Job Properties is required as below
     */
    private static final String jobPropertiesFile = "invoice/job.properties";

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
    private static final String actualDataPath = "src/test/resources/invoice/actual";
    private static final String expectedDataPath = "src/test/resources/invoice/expected";

    private static OozieRunner oozieRunner = null;

    /**
     *
     * ####IMPORTANT####
     * #Naming convention must be maintained for DOMAIN Specific Properties below
     * #Provide Test Data Source and Target Files for each Domain with Keys as "<DOMAIN_NAME>.test_data_source_file" AND "<DOMAIN_NAME>.test_data_target_file"
     * #Also, assumption is that the Test Data Target File is monitored by individual Domain Workflows for processing
     *
     */
    private static final String sourceDataFile = "/projects/ddsw/qa/data/cicd/test_data/invoice/ZZZZ_Invoice.xml_88888888_0001_1429071717.complete.xml";
    private static final String targetDataFile = "/projects/ddsw/qa/data/raw/nas/invoice/ZZZZ_Invoice.xml_88888888_0001_1429071717.complete";

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

        System.out.println("InvoiceOozieTest@BeforeClass...");

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
    public void testInvoiceCoreTableCount() {

        System.out.println("InvoiceOozieTest@testInvoiceCoreTableCount...");
        String expectedDataFile = expectedDataPath + "/invoice_current_table_count.csv";
        String actualDataFile = actualDataPath + "/invoice_current_table_count.csv";

        String query = "select count(*) from ddsw_qa.invoice_current where dealer_code ='ZZZZ'";

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
    public void testInvoiceCoreTableContent() {

        System.out.println("InvoiceOozieTest@testInvoiceCoreTableContent...");
        String expectedDataFile = expectedDataPath + "/invoice_current_table_content.csv";
        String actualDataFile = actualDataPath + "/invoice_current_table_content.csv";

        /**
         *
         *####IMPORTANT####
         * Timestamp fields would break the Content Comparision if they represents current time or change dynamically.
         * So, it may be okay to just skip the Timestamp fields from Select Queries if possible!!!
         *
         */

        String query = "select " +
                "gen_id, dealer_cust_number, dealer_invoice_num, store_number, eqp_mfr_cd, eqp_mfr_mdl, eqp_mfr_sr_no, customer_po, " +
                "tot_invc_amt, currency_code, sales_method, sales_rep_num, division_code, wo_number, invc_updt_ind, dms_system, dms_version, " +
                "transmit_by,lst_updt_by_id, file_name, orig_invoice_dt, invoice_dt, tax_misc_amt, adj_amt, line_type, line_tot_invc_amt,  " +
                "pct_cust_billed, trans_cd, serv_cntrct_num, serv_cntrct_rev_num, invoice_lvl1_no, invoice_lvl2_no, line_adj_amt, flat_quote, tot_qty, " +
                "part_number, supply_src, list_price, list_curr, net_price, net_curr, part_number_ind, maj_cls_cd, min_cls_cd, ppc_cd, parts_order_num, " +
                "tot_labor_hrs, bill_rate, bill_curr, description, app_id, app_fld, app_val, key_dealer_customer_number, key_invoice_number " +
                "key_wo_number, dealer_code "+
                " from ddsw_qa.invoice_current where dealer_code ='ZZZZ'";

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
        System.out.println("InvoiceOozieTest@AfterClass...");

        try {

            //Delete Actual Data Path where Test Result Files were stored
            oozieRunner.deleteDirectoryContent(actualDataPath);

            //Delete Fake Dealer Partition in Each Table the Workflow has created Fake Data in
            oozieRunner.dropPartition("invoice_core", "dealer_code='ZZZZ'");

            oozieRunner.dropPartition("invoice_current", "dealer_code='ZZZZ'");

        } catch (OozieClientException e) {
            e.printStackTrace();
        }
    }
}
