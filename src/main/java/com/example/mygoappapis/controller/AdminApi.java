package com.example.mygoappapis.controller;

import com.example.mygoappapis.dao.ConnectionManager;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.mygoappapis.controller.CommonMethods.*;

@RestController
@RequestMapping("/api/admin")
public class AdminApi {

    ConnectionManager connectionA = new ConnectionManager();
    DataSource web_client = connectionA.setUp();
    Connection connectionAdmin = web_client.getConnection();

    private static String UPLOADED_FOLDER = "/sample/test/";

    public AdminApi() throws Exception {
    }


    public String saveFile(MultipartFile imagefile1, String tablename, String folder) throws IOException {

        File files = new File(UPLOADED_FOLDER + "/" + tablename + "/" + folder);
        if (!files.exists()) {
            if (files.mkdirs()) {
            } else {

            }
        } else {
            // FileUtils.cleanDirectory(new java.io.File(UPLOADED_FOLDER+"/"+tablename+"/"+folder));
        }
        //for (MultipartFile imagefile : imagefile1) {

        if (imagefile1.isEmpty()) {
        }

        String oldfilename = imagefile1.getOriginalFilename();
        byte[] bytes = imagefile1.getBytes();
        Path path = Paths.get(UPLOADED_FOLDER + "/" + tablename + "/" + folder + "/" + oldfilename);
        Files.write(path, bytes);

        return "success";
    }


    @RequestMapping(value = "/adminRegister", method = RequestMethod.POST)
    public String AdminRegistration(@RequestParam String mailId, @RequestParam String mobile, @RequestParam String address, @RequestParam String companyName, @RequestParam("logo") MultipartFile companyLogo, @RequestParam("license") MultipartFile companyLicense) throws SQLException
    {
        JSONObject jsonkobj = new JSONObject();
        PreparedStatement prstmt= null;
        int password = otpGenerate();

        String template="";
        String updateTableSQL = "INSERT into adminregistrationtable(mailId, mobile, address, companyName, password) values(?,?,?,?,?)";
        try {
            prstmt = connectionAdmin.prepareStatement(updateTableSQL);
            prstmt.setString(1, mailId);
            prstmt.setString(2, mobile);
            prstmt.setString(3, address);
            prstmt.setString(4, companyName);
            prstmt.setString(5, String.valueOf(password));

            // execute update SQL stetement
            prstmt.executeUpdate();
            sendMail(mailId, "Mygo Registration", template);


            String fileresult = saveFile(companyLogo,"admin", companyName);
            String fileresult1 = saveFile(companyLicense,"admin", companyName);
            if(fileresult.equals("success") && fileresult1.equals("success")) {
                jsonkobj.put("result", "success");
            }
            else
            {
                jsonkobj.put("result", "Some problem with file size or file format");
            }
            return String.valueOf(jsonkobj);

        } catch (SQLException e) {
            jsonkobj.put("result", "failure");
            e.printStackTrace();
            return String.valueOf(jsonkobj);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (prstmt != null) {
                prstmt.close();
            }
        }
        jsonkobj.put("result","failure");
        return String.valueOf(jsonkobj);
    }

    @RequestMapping(value = "/adminLogout", method = RequestMethod.POST)
    public String AdminLogoutApi(@RequestParam String restApiKey) throws SQLException
    {
        Statement sqlSelectadminLogoutStmt = connectionAdmin.createStatement();
        JSONObject jsonObj = new JSONObject();
        PreparedStatement pstmt = null;

        String sqlLogout = "select * from adminregistrationtable where restapikey='"+restApiKey+"'";
        ResultSet rsqlLogout = sqlSelectadminLogoutStmt.executeQuery(sqlLogout);
        if(rsqlLogout.next())
        {
            jsonObj.put("result", "success");
            return String.valueOf(jsonObj);

        } else {
            jsonObj.put("result", "failure");
            return String.valueOf(jsonObj);
        }

    }

    @RequestMapping(value="/adminLogin", method = RequestMethod.POST)
    public String AdminLogin(@RequestParam String emailId, @RequestParam String password, @RequestParam String status) throws SQLException
    {
        Statement adminLoginStmt = connectionAdmin.createStatement();
        PreparedStatement preparedStatementupdatestatus = null;

        JSONObject jsonObjk = new JSONObject();
        String sqlLogin  = "select * from adminregistrationtable where mailId='"+emailId+"' AND password='"+password+"'";
        ResultSet rsetLogin = adminLoginStmt.executeQuery(sqlLogin);
        if(rsetLogin.next())
        {
            String updateTableSQL = "UPDATE adminregistrationtable SET status = ? WHERE emailId = ?";
            try {
                preparedStatementupdatestatus = connectionAdmin.prepareStatement(updateTableSQL);
                preparedStatementupdatestatus.setString(1, status);
                preparedStatementupdatestatus.setString(2, emailId);

                // execute update SQL stetement
                preparedStatementupdatestatus.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {

                if (preparedStatementupdatestatus != null) {
                    preparedStatementupdatestatus.close();
                }
            }
            jsonObjk.put("result", "success");
            jsonObjk.put("restApiKey", rsetLogin.getString("restApiKey"));
            return String.valueOf(jsonObjk);
        }
        else
        {
            jsonObjk.put("result","failure");
            return String.valueOf(jsonObjk);
        }
    }

    @RequestMapping(value = "/admin_forgot_pwd", method = RequestMethod.POST)
    public String ForgotPassword(@RequestParam String mailId) throws SQLException
    {
        Statement stmtForgotPwd = connectionAdmin.createStatement();
        JSONObject hjsonobj1 = new JSONObject();
        PreparedStatement preparedStatementupdatepwd = null;

        int otp = otpGenerate();
        String sqlForgotpwd = "SELECT * from adminregistrationtable where mailId='"+mailId+"'";
        ResultSet rsqlForgotPwd = stmtForgotPwd.executeQuery(sqlForgotpwd);
        if(rsqlForgotPwd.next())
        {
            String templateContent = "";
            //send mail with otp
            sendMail("saramani95@gmail.com", "OTP for Mygo Application", templateContent);
            String updateTableSQL = "UPDATE adminregistrationtable SET otp = ? WHERE emailId = ?";
            try {
                preparedStatementupdatepwd = connectionAdmin.prepareStatement(updateTableSQL);
                preparedStatementupdatepwd.setString(1, String.valueOf(otp));
                preparedStatementupdatepwd.setString(2, mailId);

                // execute update SQL stetement
                preparedStatementupdatepwd.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {

                if (preparedStatementupdatepwd != null) {
                    preparedStatementupdatepwd.close();
                }
            }

            hjsonobj1.put("result","success");
            return String.valueOf(hjsonobj1);
        }
        else
        {
            hjsonobj1.put("result","failure");
            return String.valueOf(hjsonobj1);
        }
    }

    @RequestMapping(value = "/admin_resend_otp", method = RequestMethod.POST)
    public String resendOtp(@RequestParam String mailId) throws SQLException
    {
        Statement stmtotpPwd = connectionAdmin.createStatement();
        JSONObject hjsonobj = new JSONObject();
        int otp = otpGenerate();
        String sqlForgotpwd = "SELECT * from adminregistrationtable where emailId='"+mailId+"'";
        ResultSet rsqlForgotPwd = stmtotpPwd.executeQuery(sqlForgotpwd);
        if(rsqlForgotPwd.next())
        {
            String templateContent = "";
            //send mail with otp
            sendMail("saramani95@gmail.com", "OTP for Mygo Application", templateContent);
            hjsonobj.put("result","success");
            return String.valueOf(hjsonobj);
        }
        else
        {
            hjsonobj.put("result","failure");
            return String.valueOf(hjsonobj);
        }
    }

    @RequestMapping(value = "/adminNewPassword", method = RequestMethod.POST)
    public String setadminnewPassword(@RequestParam String emailId, @RequestParam String newpassword) throws SQLException
    {
        Statement changeStmt = connectionAdmin.createStatement();
        JSONObject changeJsonObject = new JSONObject();
        PreparedStatement preparedStatement = null;

        String sqlChangePwd = "select * from adminregistrationtable where emailId='" + emailId + "'";
        ResultSet resset1 = changeStmt.executeQuery(sqlChangePwd);
        if (resset1.next()) {
            String updateTableSQL = "UPDATE adminregistrationtable SET password = ? WHERE emailId = ?";

            try {
                preparedStatement = connectionAdmin.prepareStatement(updateTableSQL);

                preparedStatement.setString(1, newpassword);
                preparedStatement.setString(2, emailId);

                // execute update SQL stetement
                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {

                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
            changeJsonObject.put("result", "success");
            return String.valueOf(changeJsonObject);

        } else {
            changeJsonObject.put("result", "failure");
            return String.valueOf(changeJsonObject);
        }

    }

    @RequestMapping(value="/approvedShopList", method=RequestMethod.POST)
    public String approvedShopList1(@RequestParam String restApiKey) throws SQLException
    {
        JSONObject approvedJsonObject = new JSONObject();
        JSONArray jsonarray = new JSONArray();
        Statement sqlSelectadminApprovedStmt = connectionAdmin.createStatement();
        Statement sqlSelectadminApprovedStmt1 = connectionAdmin.createStatement();
        String sqlStmtapproved = "select * from adminregistrationtable where restapikey='"+restApiKey+"'";
        ResultSet rsqlStmt = sqlSelectadminApprovedStmt.executeQuery(sqlStmtapproved);
        if(rsqlStmt.next())
        {
            String sqlSelectUsers = "select * from registrationtable where regStatus=1";
            ResultSet rsety = sqlSelectadminApprovedStmt1.executeQuery(sqlSelectUsers);
            while(rsety.next())
            {
                JSONObject newJson = new JSONObject();
                newJson.put("ShopId", rsety.getString("shopId"));
                newJson.put("mobile", rsety.getString("mobileNo"));
                newJson.put("emailId", rsety.getString("emailId"));
                newJson.put("address", rsety.getString("address"));

                jsonarray.put(newJson);
            }
            approvedJsonObject.put("result", jsonarray);
            return String.valueOf(approvedJsonObject);
        }
        else
        {
            approvedJsonObject.put("result", "failure");
            return String.valueOf(approvedJsonObject);
        }

    }

    @RequestMapping(value="/approvalShopList", method=RequestMethod.POST)
    public String approvalShopList1(@RequestParam String restApiKey) throws SQLException
    {
        JSONObject approvalJsonObject = new JSONObject();
        JSONArray jsonarray1 = new JSONArray();
        Statement sqlSelectadminApprovalStmt = connectionAdmin.createStatement();
        Statement sqlSelectadminApprovalStmt1 = connectionAdmin.createStatement();
        String sqlStmtapproval = "select * from adminregistrationtable where restapikey='"+restApiKey+"'";
        ResultSet rsqlStmt = sqlSelectadminApprovalStmt.executeQuery(sqlStmtapproval);
        if(rsqlStmt.next())
        {
            String sqlSelectUsers = "select * from registrationtable where regStatus=0";
            ResultSet rsety = sqlSelectadminApprovalStmt1.executeQuery(sqlSelectUsers);
            while(rsety.next())
            {
                JSONObject newJson = new JSONObject();
                newJson.put("ShopId", rsety.getString("shopId"));
                newJson.put("mobile", rsety.getString("mobileNo"));
                newJson.put("emailId", rsety.getString("emailId"));
                newJson.put("address", rsety.getString("address"));

                jsonarray1.put(newJson);
            }
            approvalJsonObject.put("result", jsonarray1);
            return String.valueOf(approvalJsonObject);
        }
        else
        {
            approvalJsonObject.put("result", "failure");
            return String.valueOf(approvalJsonObject);
        }

    }

    @RequestMapping(value="/approveStatus", method = RequestMethod.POST)
    public String approveStatus(@RequestParam String shopId, @RequestParam String restapikey) throws SQLException
    {
        JSONObject jsonspprove = new JSONObject();
        Statement sqlSelectadminApprovalStmt1 = connectionAdmin.createStatement();
        String sqlStmtapproval = "select * from adminregistrationtable where restapikey='"+restapikey+"'";
        ResultSet rset1 = sqlSelectadminApprovalStmt1.executeQuery(sqlStmtapproval);
        if(rset1.next())
        {
            PreparedStatement stmt=connectionAdmin.prepareStatement("update registrationtable set regStatus=? where shopId=? AND restapikey=?");
            stmt.setString(1, "1");//1 specifies the first parameter in the query i.e. name
            stmt.setString(2, shopId);
            stmt.setString(3, restapikey);
            jsonspprove.put("result","success");
            return String.valueOf(jsonspprove);
        }
        else
        {
            jsonspprove.put("result","failure");
            return String.valueOf(jsonspprove);
        }
    }
    @RequestMapping(value="/adminChangePassword", method = RequestMethod.POST)
    public String changePasswordApi(@RequestParam String oldpassword, @RequestParam String newpassword, @RequestParam String restApiKey) throws SQLException
    {
        Statement changeStmt = connectionAdmin.createStatement();
        JSONObject changeJsonObject = new JSONObject();
        PreparedStatement preparedStatement = null;
        String templatechangepwd = "";

        String sqlChangePwd = "select * from adminregistrationtable where password='" + oldpassword + "' AND restapikey='" + restApiKey + "'";
        ResultSet resset = changeStmt.executeQuery(sqlChangePwd);
        if (resset.next()) {
            String email = resset.getString("emailId");
            String updateTableSQL = "UPDATE adminregistrationtable SET password = ? WHERE restapikey = ?";

            try {
                preparedStatement = connectionAdmin.prepareStatement(updateTableSQL);

                preparedStatement.setString(1, newpassword);
                preparedStatement.setString(2, restApiKey);

                // execute update SQL stetement
                preparedStatement.executeUpdate();
                sendMail(email, "Password has been changed", templatechangepwd);


            } catch (SQLException e) {
                e.printStackTrace();
            } finally {

                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
            changeJsonObject.put("result", "success");
            return String.valueOf(changeJsonObject);

        } else {
            changeJsonObject.put("result", "failure");
            return String.valueOf(changeJsonObject);
        }
    }

    @RequestMapping(value="/adminOtpToPassword", method = RequestMethod.POST)
    public String otpToPassword(@RequestParam String otp, @RequestParam String mailId) throws SQLException
    {
        Statement stmtCheckotp = connectionAdmin.createStatement();
        JSONObject jsonobj1 = new JSONObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String sqlotptopwd = "SELECT otp,otptopwd_time  from adminregistrationtable where emailId='"+mailId+"'";
        ResultSet rsetotp = stmtCheckotp.executeQuery(sqlotptopwd);
        if(rsetotp.next())
        {
            String otp1 = rsetotp.getString("otp");
            String otptopwd_time = rsetotp.getString("otptopwd_time");
            Date date = new Date();
            String newDate = formatter.format(date);
            if(!otp1.equals("0"))
            {
                Date d1 = null;
                Date d2 = null;

                try {
                    d1 = formatter.parse(otptopwd_time);
                    d2 = formatter.parse(newDate);

                    DateTime dt1 = new DateTime(d1);
                    DateTime dt2 = new DateTime(d2);

                    int minutes = Minutes.minutesBetween(dt1, dt2).getMinutes() % 60;
                    if(minutes>=5)
                    {
                        jsonobj1.put("result", "OTP time has been expired. Try again!");
                        return String.valueOf(jsonobj1);
                    }
                    else
                    {
                        if(otp.equals(otp1))
                        {
                            jsonobj1.put("result","success");
                            return String.valueOf(jsonobj1);
                        }
                        else
                        {
                            jsonobj1.put("result", "OTP is wrong");
                            return String.valueOf(jsonobj1);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    jsonobj1.put("result", "failure");
                    return String.valueOf(jsonobj1);
                }
            }
            else
            {
                jsonobj1.put("result", "failure");
                return String.valueOf(jsonobj1);
            }
        }
        else
        {
            jsonobj1.put("result", "failure");
            return String.valueOf(jsonobj1);
        }
    }

    @RequestMapping(value = "/viewApi", method = RequestMethod.POST)
    public String viewApiDetails(@RequestParam String shopId, @RequestParam String restApiKey) throws SQLException {
        Statement StmtView = connectionAdmin.createStatement();
        Statement StmtView1 = connectionAdmin.createStatement();
        JSONObject alldetails = new JSONObject();
        String validateAdmin = "SELECT * from adminregistrationtable where restApiKey='"+restApiKey+"'";
        ResultSet gest = StmtView1.executeQuery(validateAdmin);
        if(gest.next()) {
            String sqlViewApi = "SELECT * from registrationTable where shopId='" + shopId + "'";
            ResultSet rsetu = StmtView.executeQuery(sqlViewApi);
            if (rsetu.next()) {
                JSONObject alldet = new JSONObject();
                alldetails.put("result", "success");
                alldet.put("shopId", rsetu.getString("shopId"));
                alldet.put("mobileNo", rsetu.getString("mobileNo"));
                alldet.put("address", rsetu.getString("address"));
                alldet.put("emailId", rsetu.getString("emailId"));
                alldetails.put("userData", alldet);
                return String.valueOf(alldetails);
            } else {
                alldetails.put("result", "failure");
                return String.valueOf(alldetails);
            }
        }
        else
        {
            alldetails.put("result", "failure");
            return String.valueOf(alldetails);
        }
    }

    @RequestMapping(value = "/adminDashboard",  method = RequestMethod.POST)
    public String AdminDashboard(@RequestParam String restApiKey) throws SQLException {

        JSONObject jsonbj = new JSONObject();
        Statement dashboardStmt = connectionAdmin.createStatement();
        Statement dashboardStmt1 = connectionAdmin.createStatement();
        Statement dashboardStmt2 = connectionAdmin.createStatement();
        String sqlcount = "SELECT * from adminregistrationtable where restApiKey='"+restApiKey+"'";
        ResultSet rset = dashboardStmt.executeQuery(sqlcount);
        if(rset.next())
        {
            String sqlCheck = "SELECT count(*) from registrationTable";
            ResultSet totalusers = dashboardStmt1.executeQuery(sqlCheck);
            if(totalusers.next())
            {
                jsonbj.put("totalUsers", totalusers.getString(1));
            }

            String sqlApprovedusers = "SELECT count(*) from registrationTable where regStatus=1";
            ResultSet approvedusers = dashboardStmt2.executeQuery(sqlApprovedusers);
            if(approvedusers.next())
            {
                jsonbj.put("approvedUsers", approvedusers.getString(1));
            }
            jsonbj.put("result", "success");
            return String.valueOf(jsonbj);
        }
        else
        {
            jsonbj.put("result","failure");
            return String.valueOf(jsonbj);
        }

    }

   /*@RequestMapping(value = "/transcition_history", method = RequestMethod.POST)
    public String transcitionHitory(@RequestParam String shopId, @RequestParam String restApiKey)
    {

    }*/
}
