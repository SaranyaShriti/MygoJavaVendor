package com.example.mygoappapis.controller;

import com.example.mygoappapis.dao.ConnectionManager;
import com.example.mygoappapis.dao.ConnectionManagerDriver;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.mygoappapis.controller.CommonMethods.*;

@RestController
@RequestMapping("api/user")
public class UserApi {


    ConnectionManager connectionA = new ConnectionManager();
    DataSource web_client = connectionA.setUp();
    Connection connection = web_client.getConnection();

    ConnectionManagerDriver connectionB = new ConnectionManagerDriver();
    DataSource web_client1 = connectionB.setUp();
    Connection connectionDriver = web_client1.getConnection();

    public UserApi() throws Exception {
    }

    @RequestMapping(value="/registration", method = RequestMethod.POST)
    public String register(@RequestParam String shopId, @RequestParam String mobile, @RequestParam String address, @RequestParam String emailId, @RequestParam String branchName, @RequestParam String companyName, @RequestParam String managerName, @RequestParam String notifyId) throws SQLException
    {
        JSONObject resultJson = new JSONObject();
        String template = "";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //System.out.println(formatter.format(new Date()));
        Statement stmt = connection.createStatement();
        Statement stmtd = connection.createStatement();
        if (shopId!=null && mobile!=null && address!=null && emailId != null && !notifyId.equals("0")) {
            String sqlSelect = "SELECT shopId from registrationTable where shopId='" + shopId + "'";
            ResultSet rsqlSelect = stmt.executeQuery(sqlSelect);
            if (rsqlSelect.next()) {
                resultJson.put("result", "User for this ShopId already exists");
                return String.valueOf(resultJson);

            } else {

                String sqlRegisterDetailsInsert = "INSERT into registrationTable(shopId,mobileNo,address,emailId,notifyId,regStatus,registered_datetime,branchName, companyName, managerName) values(?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement stmt1 = connection.prepareStatement(sqlRegisterDetailsInsert);
                stmt1.setString(1, shopId);
                stmt1.setString(2, mobile);
                stmt1.setString(3, address);
                stmt1.setString(4, emailId);
                stmt1.setString(5, notifyId);
                stmt1.setInt(6, 0);
                stmt1.setString(7, formatter.format(new Date()));
                stmt1.setString(8, branchName);
                stmt1.setString(9, companyName);
                stmt1.setString(10, managerName);

                stmt1.executeUpdate();
                sendMail(emailId, "Mygo Registration", template);
                resultJson.put("result", "success");
                return String.valueOf(resultJson);
            }

        }
        else
        {
            resultJson.put("result", "failure");
            return String.valueOf(resultJson);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginApi(@RequestParam String shopId, @RequestParam String password, @RequestParam String notifyId) throws SQLException
    {
        Statement stmtlogin = connection.createStatement();
        JSONObject resultJson1 = new JSONObject();
        if(shopId!=null && !notifyId.equals("0")) {
            String restapikey1 = generateSessionKey(12);
            String newkey= shopId+restapikey1;
            String sqlSelect1 = "SELECT * from registrationTable where (shopId='" + shopId + "' AND password='" + password + "') AND (notifyId='" + notifyId + "' AND regStatus='1')";
            ResultSet rsset = stmtlogin.executeQuery(sqlSelect1);
            if (rsset.next()) {
                PreparedStatement pstmt = connection.prepareStatement("UPDATE registrationTable SET restapikey = ? WHERE shopId = ?");
                pstmt.setString(1, newkey);
                pstmt.setString(2, shopId);
                resultJson1.put("result", "success");
                resultJson1.put("restapikey", newkey);
                return String.valueOf(resultJson1);
            } else {
                resultJson1.put("result", "failure");
                return String.valueOf(resultJson1);
            }
        }
        else
        {
            resultJson1.put("result", "failure");
            return String.valueOf(resultJson1);
        }
    }

    @RequestMapping(value="/changePassword", method = RequestMethod.POST)
    public String changePasswordApi(@RequestParam String oldpassword, @RequestParam String newpassword, @RequestParam String restApiKey) throws SQLException
    {
        Statement changeStmt = connection.createStatement();
        JSONObject changeJsonObject = new JSONObject();
        PreparedStatement preparedStatement = null;
        String templatechangepwd = "";

        String sqlChangePwd = "select * from registrationTable where password='" + oldpassword + "' AND restapikey='" + restApiKey + "'";
        ResultSet resset = changeStmt.executeQuery(sqlChangePwd);
        if (resset.next()) {
            String email = resset.getString("emailId");
            String updateTableSQL = "UPDATE registrationTable SET password = ? WHERE restapikey = ?";

            try {
                preparedStatement = connection.prepareStatement(updateTableSQL);

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

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logoutApi(@RequestParam String restApiKey) throws SQLException
    {
        Statement sqlSelectLogoutStmt = connection.createStatement();
        JSONObject jsonObj = new JSONObject();
        PreparedStatement pstmt = null;

        String sqlLogout = "select * from registrationTable where restapikey='"+restApiKey+"'";
        ResultSet rsqlLogout = sqlSelectLogoutStmt.executeQuery(sqlLogout);
        if(rsqlLogout.next())
        {

            String updateLogoutStatus = "UPDATE registrationTable set notifyId='0' where restapikey=?";
            pstmt = connection.prepareStatement(updateLogoutStatus);

            pstmt.setString(1, restApiKey);
            pstmt.executeUpdate();

            jsonObj.put("result", "success");
            return String.valueOf(jsonObj);

        } else {
            jsonObj.put("result", "failure");
            return String.valueOf(jsonObj);
        }

    }

    @RequestMapping(value = "/forgot_password", method = RequestMethod.POST)
    public String forgotPassword(@RequestParam String shopIdOrmailId) throws SQLException
    {
        Statement stmtForgotPwd = connection.createStatement();
        JSONObject hjsonobj = new JSONObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        PreparedStatement preparedStatementupdate = null;

        int otp = otpGenerate();
        String sqlForgotpwd = "SELECT * from registrationTable where emailId='"+shopIdOrmailId+"' OR shopId='"+shopIdOrmailId+"'";
        ResultSet rsqlForgotPwd = stmtForgotPwd.executeQuery(sqlForgotpwd);
        if(rsqlForgotPwd.next())
        {
            String templateContent = "";
            String useremail = rsqlForgotPwd.getString("emailId");
            //send mail with otp
            sendMail("saramani95@gmail.com", "OTP for Mygo Application", templateContent);
            String updateTableSQL = "UPDATE registrationTable SET otp = ?,otptopwd_time=?  WHERE emailId = ?";

            try {

                preparedStatementupdate = connection.prepareStatement(updateTableSQL);
                preparedStatementupdate.setString(1, String.valueOf(otp));
                preparedStatementupdate.setString(2, formatter.format(new Date()));
                preparedStatementupdate.setString(3, useremail);

                // execute update SQL stetement
                preparedStatementupdate.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {

                if (preparedStatementupdate != null) {
                    preparedStatementupdate.close();
                }
            }
            hjsonobj.put("result","success");
            return String.valueOf(hjsonobj);
        }
        else
        {
            hjsonobj.put("result","failure");
            return String.valueOf(hjsonobj);
        }
    }

    @RequestMapping(value="/otpToPassword", method = RequestMethod.POST)
    public String otpToPassword(@RequestParam String otp, @RequestParam String mailId) throws SQLException
    {
        Statement stmtCheckotp = connection.createStatement();
        JSONObject jsonobj = new JSONObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String sqlotptopwd = "SELECT otp,otptopwd_time  from registrationTable where emailId='"+mailId+"'";
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
                        jsonobj.put("result", "OTP time has been expired. Try again!");
                        return String.valueOf(jsonobj);
                    }
                    else
                    {
                        if(otp.equals(otp1))
                        {
                            jsonobj.put("result","success");
                            return String.valueOf(jsonobj);
                        }
                        else
                        {
                            jsonobj.put("result", "OTP is wrong");
                            return String.valueOf(jsonobj);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    jsonobj.put("result", "failure");
                    return String.valueOf(jsonobj);
                }
            }
            else
            {
                jsonobj.put("result", "failure");
                return String.valueOf(jsonobj);
            }
        }
        else
        {
            jsonobj.put("result", "failure");
            return String.valueOf(jsonobj);
        }
    }

    @RequestMapping(value = "/resend_otp", method = RequestMethod.POST)
    public String resendOtp(@RequestParam String shopIdOrmailId) throws SQLException
    {
        Statement stmtotpPwd = connection.createStatement();
        JSONObject hjsonobj = new JSONObject();
        int otp = otpGenerate();
        String sqlForgotpwd = "SELECT * from registrationtable where emailId='"+shopIdOrmailId+"' OR shopId='"+shopIdOrmailId+"'";
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

    @RequestMapping(value = "/new_password", method = RequestMethod.POST)
    public String setnewPassword(@RequestParam String emailId, @RequestParam String newpassword) throws SQLException
    {
        Statement changeStmt = connection.createStatement();
        JSONObject changeJsonObject = new JSONObject();
        PreparedStatement preparedStatement = null;

        String sqlChangePwd = "select * from registrationTable where emailId='" + emailId + "'";
        ResultSet resset1 = changeStmt.executeQuery(sqlChangePwd);
        if (resset1.next()) {
            String updateTableSQL = "UPDATE registrationTable SET password = ? WHERE emailId = ?";

            try {
                preparedStatement = connection.prepareStatement(updateTableSQL);

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

    @RequestMapping(value="/userProfile", method = RequestMethod.POST)
    public String userProfileUrl(@RequestParam String restApiKey) throws SQLException
    {
        JSONObject profileJson = new JSONObject();
        Statement statement = connection.createStatement();

        String sql = "SELECT * from registrationTable where restapikey='"+restApiKey+"'";
        ResultSet rsetful = statement.executeQuery(sql);
        if(rsetful.next())
        {
            JSONObject jnon = new JSONObject();
            jnon.put("mobile", rsetful.getString("mobileNo"));
            jnon.put("emailId", rsetful.getString("emailId"));
            jnon.put("shopId", rsetful.getString("shopId"));
            profileJson.put("result", "success");
            profileJson.put("userData", jnon);
            return String.valueOf(profileJson);
        }
        else
        {
            profileJson.put("result", "failure");
            return String.valueOf(profileJson);
        }
    }

    @RequestMapping(value="/dashboard", method = RequestMethod.POST)
    public String dashboard(@RequestParam String latitude, @RequestParam String longitude, @RequestParam String restapi, @RequestParam String shopId) throws SQLException
    {
        Statement stmtDashboard = connectionDriver.createStatement();
        Statement checkApi = connection.createStatement();
        JSONArray jarr = new JSONArray();
        JSONObject njsonj = new JSONObject();

        String usersql = "SELECT * from registrationTable where restapikey='"+restapi+"' AND shopId='"+shopId+"'";
        ResultSet Stmtcheck = checkApi.executeQuery(usersql);
        if(Stmtcheck.next()) {
            String sql = "SELECT d.id as id, d.nama_depan, d.nama_belakang, ld.latitude, ld.longitude, ld.update_at,\n" +
                    "            k.merek, k.nomor_kendaraan, k.warna, k.tipe, s.saldo, ub.nominal as budget_belanja,\n" +
                    "            d.no_telepon, CONCAT('$url_foto', d.foto, '') as foto, d.reg_id, dj.driver_job,\n" +
                    "                (6371 * acos(cos(radians(" + latitude + ")) * cos(radians( ld.latitude )) * cos(radians(ld.longitude) - radians(" + longitude + ")) + sin(radians(" + latitude + ")) * sin( radians(ld.latitude)))) AS distance\n" +
                    "            FROM config_driver ld, driver d, driver_job dj, kendaraan k,\n" +
                    "                saldo s, config_driver cd, uang_belanja ub\n" +
                    "            WHERE ld.id_driver = d.id \n" +
                    "                AND ld.status = '1'\n" +
                    "                AND dj.id = d.job\n" +
                    "                AND d.job = '1'\n" +
                    "                AND d.status = '1'\n" +
                    "                AND k.id = d.kendaraan\n" +
                    "                AND s.id_user = d.id\n" +
                    "                AND s.saldo > 3      \n" +
                    "                AND cd.id_driver = d.id\n" +
                    "                AND ub.id = cd.uang_belanja\n" +
                    "            HAVING distance <= 10\n" +
                    "            ORDER BY distance ASC";


            ResultSet rsetqueryfetch = stmtDashboard.executeQuery(sql);
            if (rsetqueryfetch.next()) {
                JSONObject onejson = new JSONObject();
                onejson.put("id", rsetqueryfetch.getString("id"));
                onejson.put("name", rsetqueryfetch.getString("nama_depan") + " " + rsetqueryfetch.getString("nama_belakang"));
                onejson.put("latitude", rsetqueryfetch.getString("latitude"));
                onejson.put("longitude", rsetqueryfetch.getString("longitude"));
                onejson.put("update_at", rsetqueryfetch.getString("update_at"));
                onejson.put("brand", rsetqueryfetch.getString("merek"));
                onejson.put("route_type", rsetqueryfetch.getString("nomor_kendaraan"));
                onejson.put("color", rsetqueryfetch.getString("warna"));
                onejson.put("type", rsetqueryfetch.getString("tipe"));
                onejson.put("balance", rsetqueryfetch.getString("saldo"));
                onejson.put("shopping budget", rsetqueryfetch.getString("budget_belanja"));
                onejson.put("phone", rsetqueryfetch.getString("no_telepon"));
                onejson.put("photo", rsetqueryfetch.getString("foto"));
                onejson.put("reg_id", rsetqueryfetch.getString("reg_id"));
                onejson.put("driver_job", rsetqueryfetch.getString("driver_job"));
                onejson.put("distance", rsetqueryfetch.getString("distance"));

                jarr.put(onejson);
                njsonj.put("result", jarr);


            }
            else
            {
                njsonj.put("result", "No driver found");
            }
        }
        else
        {
            njsonj.put("result","failure");
        }

        return String.valueOf(njsonj);

    }

    @RequestMapping(value = "/trackDriver", method = RequestMethod.POST)
    public String track_Driver_Location(@RequestParam String driverId) throws SQLException
    {
        Statement connectionLocStmt = connectionDriver.createStatement();
        JSONObject driverJson1 = new JSONObject();
        String sql = "SELECT * from config_driver where id_driver='"+driverId+"'";
        ResultSet rest = connectionLocStmt.executeQuery(sql);
        if(rest.next())
        {
            JSONObject driverJson = new JSONObject();
            driverJson.put("latitude", rest.getString("latitude"));
            driverJson.put("longitude", rest.getString("longitude"));
            driverJson.put("update_at", rest.getString("update_at"));
            driverJson1.put("result", driverJson);
            return String.valueOf(driverJson1);
        }
        else
        {
            driverJson1.put("result","failure");
            return String.valueOf(driverJson1);
        }
    }

    @RequestMapping(value="/orderRequest", method = RequestMethod.POST)
    public String orderRequest(@RequestParam String latitude, @RequestParam String longitude, @RequestParam String ShopId, @RequestParam String restApiKey) throws SQLException
    {
        Statement connectionStmt = connection.createStatement();
        Statement connectionStmt1 = connectionDriver.createStatement();

        JSONObject jsonobj = new JSONObject();
        JSONArray jarr = new JSONArray();

        int bookingId = otpGenerate();
        String subshopId=ShopId.substring(0,2);
        String generateBookingId = "SELECT branchId,branchName from registrationTable where shopId='"+ShopId+"'";
        ResultSet resultSet1 = connectionStmt.executeQuery(generateBookingId);
        if(resultSet1.next())
        {
            String cbookingId = subshopId+resultSet1.getString("branchId")+bookingId;

            jsonobj.put("result", "success");
            jsonobj.put("BookingID", cbookingId);

        }
        else
        {
            jsonobj.put("result","failure");
            return String.valueOf(jsonobj);
        }

        String sqlDriver = "SELECT driver.nama_depan, driver.nama_belakang, driver.no_telepon, driver.id, driver.reg_id FROM config_driver, driver WHERE driver.id=config_driver.id_driver AND config_driver.status=2";
        ResultSet rsset = connectionStmt1.executeQuery(sqlDriver);
        while (rsset.next())
        {
            String drivername = rsset.getString("nama_depan")+rsset.getString("nama_belakang");
            String phoneno = rsset.getString("no_telepon");
            String id = rsset.getString("id");
            String reg_id = rsset.getString("reg_id");
            JSONObject driverObj = new JSONObject();
            driverObj.put("Driver_name", drivername);
            driverObj.put("Driver_ID", id);
            driverObj.put("Driver_mobile_number", phoneno);
            driverObj.put("Driver_FCM_ID", reg_id);
            jarr.put(driverObj);
        }

        jsonobj.put("Driver_Info", jarr);
        return String.valueOf(jsonobj);
    }

    @RequestMapping(value = "/takeOrder", method = RequestMethod.POST)
    public String takeOrderRequest(@RequestParam String shopID, @RequestParam String shopBranchName, @RequestParam String mobileNumber, @RequestParam String shopFCMID,@RequestParam String latitude, @RequestParam String longitude, @RequestParam String managerName, @RequestParam String DriverName, @RequestParam String DriverID, @RequestParam String driverMobileNumber, @RequestParam String driverFCMID, @RequestParam String DriverStatus, @RequestParam String shopRestapikey, @RequestParam String BookingID, @RequestParam String BookingStatus) throws SQLException
    {

        Statement takeOrderStmt = connection.createStatement();
        Statement takeOrderStmt1 = connection.createStatement();
        JSONObject jsonObj  = new JSONObject();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String newDate = formatter.format(date);


        String sqlp = "SELECT companyName from registrationTable where shopId='"+shopID+"'";
        ResultSet rset = takeOrderStmt.executeQuery(sqlp);
        if(rset.next())
        {
            String cname = rset.getString("companyName");
            String vcode = cname.substring(0,2)+verifCode(4);
            String sqltc = "INSERT into orderDetails(shopId, branchName, phoneNo, shopFcmid, latitude, longitude, managerName, driverName, driverId, driverMobileNo, driverFcmId, driverStatus, bookingId, bookingStatus, shopRestApiKey, verifyCode, VerifyCodegendatetime) values('" + shopID + "','" + shopBranchName + "','" + mobileNumber + "','" + shopFCMID + "','" + latitude + "','" + longitude + "','" + managerName + "','" + DriverName + "','" + DriverID + "','" + driverMobileNumber + "','" + driverFCMID + "','" + DriverStatus + "','" + BookingID + "','" + BookingStatus + "','" + shopRestapikey + "', '"+vcode+"',''"+newDate+"')";
            takeOrderStmt1.executeUpdate(sqltc);

            //Bhar DB Update driver status=2



            jsonObj.put("result", "success");
            jsonObj.put("verification_code", vcode);
            return String.valueOf(jsonObj);
        }
        else
        {
            jsonObj.put("result","failure");
            return String.valueOf(jsonObj);
        }
    }

    @RequestMapping(value = "/notification", method = RequestMethod.POST)
    public String notification(@RequestParam String shopId, @RequestParam String restApiKey) throws SQLException
    {
        Statement stmtNotify = connection.createStatement();
        Statement stmtNotify1 = connection.createStatement();

        JSONObject newJsonobj = new JSONObject();
        JSONArray njobj = new JSONArray();
        String vendorApi = "SELECT * from registrationTable where restapikey='"+restApiKey+"'";
        ResultSet rvendorApi = stmtNotify1.executeQuery(vendorApi);
        if(rvendorApi.next()) {
            String sqlNotify = "SELECT * from orderDetails where shopId='" + shopId + "' AND bookingStatus='1'";
            ResultSet rsetNotify = stmtNotify.executeQuery(sqlNotify);
            while (rsetNotify.next()) {
                JSONObject jsonol = new JSONObject();
                jsonol.put("driverId", rsetNotify.getString("driverId"));
                jsonol.put("driverName", rsetNotify.getString("driverName"));
                jsonol.put("driverFCMId", rsetNotify.getString("driverFcmId"));
                jsonol.put("driverMobile", rsetNotify.getString("driverMobileNo"));
                jsonol.put("BookingId", rsetNotify.getString("bookingId"));
                njobj.put(jsonol);
            }
            newJsonobj.put("result", njobj);
            return String.valueOf(newJsonobj);
        }
        else
        {
            newJsonobj.put("result", "failure");
            return String.valueOf(newJsonobj);
        }
    }

    @RequestMapping(value = "/requestToVerify", method = RequestMethod.POST)
    public String requestToVerify(@RequestParam String verificationCode, @RequestParam String driverID, @RequestParam String bookingID, @RequestParam String restApiKey) throws SQLException
    {
        //45 minutes
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject verifyJson = new JSONObject();
        Statement stmtk = connection.createStatement();
        Statement stmtk1 = connection.createStatement();
        String sqlVerify = "SELECT * from registrationTable where restapikey='"+restApiKey+"'";
        ResultSet rsetVerify = stmtk.executeQuery(sqlVerify);
        if(rsetVerify.next())
        {
            String hsql = "SELECT * from orderDetails where bookingId='"+bookingID+"' AND driverId='"+driverID+"'";
            ResultSet rhsql = stmtk1.executeQuery(hsql);
            if(rhsql.next())
            {
                String verifCode = rhsql.getString("verifyCode");
                String verifyGenDateTime = rhsql.getString("VerifyCodegendatetime");
                Date currentDateTime = new Date();
                String newDate = formatter.format(currentDateTime);
                Date d1 = null;
                Date d2 = null;
                try {
                    d1 = formatter.parse(verifyGenDateTime);
                    d2 = formatter.parse(newDate);

                    DateTime dt1 = new DateTime(d1);
                    DateTime dt2 = new DateTime(d2);

                    int minutes = Minutes.minutesBetween(dt1, dt2).getMinutes() % 60;
                    if(minutes>=45)
                    {

                        verifyJson.put("result", "Verification time has been expired. Try again!");
                        return String.valueOf(verifyJson);
                    }
                    else
                    {
                        if(verifCode.equals(verificationCode))
                        {
                            verifyJson.put("result","success");
                            return String.valueOf(verifyJson);
                        }
                        else
                        {
                            verifyJson.put("result", "Verification code is wrong");
                            return String.valueOf(verifyJson);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    verifyJson.put("result", "failure");
                    return String.valueOf(verifyJson);
                }

            }
            else
            {
                verifyJson.put("result", "failure");
                return String.valueOf(verifyJson);

            }

        }
        else
        {
            verifyJson.put("result", "failure");
            return String.valueOf(verifyJson);
        }
    }

    @RequestMapping(value="/requestToCancel", method = RequestMethod.POST)
    public String requestToCancel(@RequestParam String driverID, @RequestParam String ShopID, @RequestParam String purposeOfCancellation, @RequestParam String restapikey) throws SQLException
    {
        Statement Stmt = connection.createStatement();

        JSONObject cancelJson = new JSONObject();
        String sqlCancel = "SELECT * from registrationTable where restapikey='"+restapikey+"'";
        ResultSet rset = Stmt.executeQuery(sqlCancel);
        if(rset.next())
        {
            String sqlRegisterDetailsInsert = "UPDATE orderDetails SET purposeOfCancel=?, bookingStatus=? where shopId=? AND driverId=? ORDER BY DESC LIMIT 1";
            PreparedStatement stmt1 = connection.prepareStatement(sqlRegisterDetailsInsert);
            stmt1.setString(1, purposeOfCancellation);
            stmt1.setString(2, "4");
            stmt1.setString(3, ShopID);
            stmt1.setString(4, driverID);
            stmt1.executeUpdate();

            //Bhar DB update status of driver =1
            cancelJson.put("result", "success");
            return String.valueOf(cancelJson);
        }
        else
        {
            cancelJson.put("result","failure");
            return String.valueOf(cancelJson);
        }
    }

    @RequestMapping(value="/customerDetails", method = RequestMethod.POST)
    public String customerdetailsInsert(@RequestParam String shopId, @RequestParam String bookingId, @RequestParam String customerName, @RequestParam String customerMobile, @RequestParam String customerAddress, @RequestParam String foodBillNumber, @RequestParam String foodItems, @RequestParam String foodCost, @RequestParam String driverId, @RequestParam String restApiKey) throws SQLException, IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Statement stmtValidate = connection.createStatement();

        JSONObject jsonObj = new JSONObject();
        Date cdate = new Date();
        String currentDate = formatter.format(cdate);
        String validateVendor = "SELECT * from registrationTable where restapikey='"+restApiKey+"'";
        ResultSet setr = stmtValidate.executeQuery(validateVendor);
        if(setr.next()) {
            final String GET_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=Anna%20University,%20Guindy,%20Chennai,%20Tamil%20Nadu%20600025&key=AIzaSyDymSbowwIDIne0IiFWf2czcoHguLmM9G0";
            String LatLongJson = addressToLatLong(GET_URL);
            String latLongvalue = fetchLatLongFromJson(LatLongJson);
            String latlongSeperateValue[] = latLongvalue.split(",");
            String stmtCustomerDetails = "INSERT into customerDetails(ShopId,BookingId,customerName,customerMobile,customerLat,customerLong,foodBillNo,foodItems,foodCost,StartdateTime,driverID) values(?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement stmt1 = connection.prepareStatement(stmtCustomerDetails);
            stmt1.setString(1, shopId);
            stmt1.setString(2, bookingId);
            stmt1.setString(3, customerName);
            stmt1.setString(4, customerMobile);
            stmt1.setString(5, latlongSeperateValue[0]);
            stmt1.setString(6, latlongSeperateValue[1]);
            stmt1.setString(7, foodBillNumber);
            stmt1.setString(8, foodItems);
            stmt1.setString(9, foodCost);
            stmt1.setString(10, currentDate);
            stmt1.setString(11, driverId);
            stmt1.executeUpdate();

            String stmtUpdateBookingStatus = "UPDATE orderDetails set bookingStatus=? where bookingId=?";
            PreparedStatement stmt2 = connection.prepareStatement(stmtUpdateBookingStatus);
            stmt1.setString(1, "2");
            stmt1.setString(2, bookingId);

            //bhar db update driver status = 3

            jsonObj.put("result", "success");
            return String.valueOf(jsonObj);
        }
        else {
            jsonObj.put("result","failure");
            return String.valueOf(jsonObj);

        }

    }

    @RequestMapping(value = "/trackOrder", method = RequestMethod.POST)
    public String trackOrder1(@RequestParam String shopId, @RequestParam String restApiKey) throws SQLException {
        Statement stmtCheckVendorStmt = connection.createStatement();
        Statement stmtCheckOrderDetails = connection.createStatement();

        JSONObject bookingJsonObj = new JSONObject();
        JSONArray bookingArray = new JSONArray();

        String sqlCheckVendor = "SELECT * from registrationTable where restapikey='"+restApiKey+"'";
        ResultSet rsetCheckVendor = stmtCheckVendorStmt.executeQuery(sqlCheckVendor);
        if(rsetCheckVendor.next())
        {
            String orderStmt = "SELECT bookingId, driverId, driverMobileNo from orderDetails where bookingStatus='2' AND shopId='"+shopId+"'";
            ResultSet rsetFetch = stmtCheckOrderDetails.executeQuery(orderStmt);
            while(rsetFetch.next())
            {
                JSONObject indivObj = new JSONObject();
                indivObj.put("DriverId", rsetFetch.getString("driverId"));
                indivObj.put("BookingId", rsetFetch.getString("bookingId"));
                indivObj.put("DriverMobile", "driverMobileNo");
                bookingArray.put(indivObj);
            }

            bookingJsonObj.put("result","success");
            bookingJsonObj.put("bookingArray",bookingArray);
            return String.valueOf(bookingJsonObj);

        }
        else
        {
            bookingJsonObj.put("result","failure");
            return String.valueOf(bookingJsonObj);
        }
    }

    /*@RequestMapping(value="/transictionHistory", method=RequestMethod.POST)
     public String thistory(@RequestParam String restApiKey, @RequestParam String shopId)
    {

    }*/



}
