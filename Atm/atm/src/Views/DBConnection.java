/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Views;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.prefs.Preferences;

/**
 *
 * @author batuhan
 */
public class DBConnection {

    Connection conn = null;
    private String query = null;
    private ResultSet rs;
    static Statement statement = null;
    String url = ("jdbc:sqlite:atm2.sqlite");

    /**
     *
     */
     private Connection connect() {   
        String url = "jdbc:sqlite:atm2.sqlite";  
        Connection conn = null;  
        try {  
            conn = DriverManager.getConnection(url);  
        } catch (SQLException e) {  
            System.out.println(e.getMessage());  
        }  
        return conn;  
    }  
    public void ekle(Musteri ms) {
        try {
            DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
            LocalDate tarih = LocalDate.now();
            
            Class.forName("org.sqlite.JDBC");
            Connection conn = this.connect();  
            statement = (Statement) conn.createStatement();
            statement.executeUpdate("INSERT INTO MUSTERI (KARTNO,ISIM,SOYAD,TC,DTARIHI,SIFRE)VALUES('" + ms.getKrtNo().trim() + "','" + ms.getAdi() + "','" + ms.getSoyadi() + "','" + ms.getTc() + "','" + ms.getDt() + "','" + ms.getSifre() + "')");
            statement.executeUpdate("INSERT INTO KARTBILGI (KARTNO,BAKIYE,ISLEMTARIHI)VALUES('" + ms.getKrtNo().trim() + "',1000.0,'" + tarih + "')");
           
            conn.close();

        } catch (Exception e) {

            System.out.println(e.getMessage());

        }

    }

    public String giris(String kno, String sifre) {
        try {
            
            Connection conn = this.connect();  
            query = "SELECT * FROM MUSTERI WHERE KARTNO='" + kno + "' AND SIFRE='" + sifre + "'";
            PreparedStatement stm = conn.prepareStatement(query);
            rs = stm.executeQuery();
            
             conn.close();
            while (rs.next()) {
                return "1";
            }
        } catch (Exception e) {
 
        }
        
        return "0";
    }

    public String isim(String kno) {
        try {
            Connection conn = this.connect();  
            query = "SELECT * FROM MUSTERI WHERE KARTNO='" + kno + "'";
            PreparedStatement stm = conn.prepareStatement(query);
            rs = stm.executeQuery();
            
            while (rs.next()) {
                String a= rs.getString("ISIM") + " " + rs.getString("SOYAD");
                conn.close();
                return a;
            }
        } catch (Exception e) {
        }
        return "";
    }

    public Integer Transfer(String gonderen, String alıcı, Double tutar, String aciklama) {
        DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate tarih = LocalDate.now();
        try {
            Connection conn = this.connect();  
            //query = "INSERT INTO MUSTERI (KARTNO,ISIM,SOYAD,TC,DTARIHI,SIFRE)VALUES('"+kno+"','"+ad+"','"+soyad+"','"+tc+"','"+dt+"','"+sifre+"')";
            // query="SELECT * FROM MUSTERI";

            statement = (Statement) conn.createStatement();
            statement.executeUpdate("INSERT INTO TRANSFERBILGI (AKTARANNO,AKTARILANNO,TARIH,ACIKLAMA,TUTAR)VALUES('" + gonderen + "','" + alıcı + "','" + tarih + "','" + aciklama + "'," + tutar + ")");
            // PreparedStatement stm = conn.prepareStatement(query);
            //rs = stm.executeQuery();
            //while (rs.next()){
            //   String userName = rs.getString("ISIM");
            //  String password = rs.getString("SOYAD");

            //   System.out.println("Username : " + userName + "\n" + "Password : " + password); 
            //  }
            conn.close();

        } catch (Exception e) {

            System.out.println(e.getMessage());

        }

        return 1;
    }

    public String TransferdeKartNoKontrol(String kno) {
        try {
            Connection conn = this.connect();  
            query = "SELECT * FROM KARTBILGI WHERE KARTNO='" + kno + "'";
            PreparedStatement stm = conn.prepareStatement(query);
            rs = stm.executeQuery();
            
             conn.close();
            while (rs.next()) {
                
                return "1";
            }
        } catch (Exception e) {

        }
        return "0";
        
    }

    public String ParaGonderimi(String kartno, double tutar) {
        String mesaj = "";
        Preferences pref = Preferences.userNodeForPackage(AnaEkran.class);
        String KartNo = pref.get("kartno", "");
        if (KartNo.equals(kartno)) {
            mesaj = "Kendi hesabınıza para gönderemezsiniz.";
        } else if (TransferdeKartNoKontrol(kartno).equals("1")) {
            double benimbakiyem = 0;
            try {
            Connection conn = this.connect();  
                query = "SELECT BAKIYE FROM KARTBILGI WHERE KARTNO='" + KartNo + "'";
                PreparedStatement stm = conn.prepareStatement(query);
                rs = stm.executeQuery();
                while (rs.next()) {
                    benimbakiyem = Double.parseDouble(rs.getString("BAKIYE"));
                }
            } catch (Exception e) {

            }

            if (benimbakiyem < tutar) {
                mesaj = "Yetersiz bakiye. En fazla " + benimbakiyem + " kadar tl gönderebilirsiniz.";
            } else {
                try {  
            Connection conn = this.connect();  
                     PreparedStatement pstmt = conn.prepareStatement("update KARTBILGI set BAKIYE = BAKIYE + " + tutar + " where KARTNO='" + kartno + "'");
                     pstmt.executeUpdate(); 
                     PreparedStatement pstmt1 = conn.prepareStatement("update KARTBILGI set BAKIYE = BAKIYE - " + tutar + " where KARTNO='" + KartNo + "'");
                     pstmt1.executeUpdate(); 
                    conn.close(); 
                    mesaj = kartno + " kart numarasına " + tutar + " TL gönderildi.";

                } catch (Exception e) {

                    System.out.println("Lütfen Gerekli Alanları Doldurunuz");

                }

            }

        } else {
            mesaj = "Kart numarası bulunamadı.";
        }

        return mesaj;
    }
    public String sifredegis(String eskisifre,String yenisifre,String Kartno)
    {
        String mesaj="0";
         try {
            Connection conn = this.connect();  
                    statement = (Statement) conn.createStatement();
                    statement.executeUpdate("update MUSTERI set SIFRE = '"+ yenisifre  + "' where KARTNO='" + Kartno + "' AND SIFRE='"+ eskisifre +"'");
                    mesaj="1";
                    conn.close(); 

                } catch (Exception e) {

                    System.out.println(e.getMessage());

                }
        return mesaj;
    }
    public String bakiye()
    {
        
        Preferences pref = Preferences.userNodeForPackage(AnaEkran.class);
        String KartNo = pref.get("kartno", "");
         try {
             
            Class.forName("org.sqlite.JDBC");
            Connection conn = connect();  
            query = "SELECT BAKIYE FROM KARTBILGI WHERE KARTNO='" + KartNo + "'";
            PreparedStatement stm = conn.prepareStatement(query);
            rs = stm.executeQuery();
            while (rs.next()) {
               String a= rs.getString("BAKIYE"); 
               conn.close();
               return a;
            }
            
        } catch (Exception e) { 
        } 
         return "0";
    }
    public String ParaCekme(Double tutar)
    {
        
        String mesaj = "";
        Preferences pref = Preferences.userNodeForPackage(AnaEkran.class);
        String KartNo = pref.get("kartno", "");
            double benimbakiyem = 0;
            try {
            Connection conn = this.connect();  
                query = "SELECT BAKIYE FROM KARTBILGI WHERE KARTNO='" + KartNo + "'";
                PreparedStatement stm = conn.prepareStatement(query);
                rs = stm.executeQuery();

                while (rs.next()) {
                    benimbakiyem = Double.parseDouble(rs.getString("BAKIYE"));  
                    conn.close();
                }
            } catch (Exception e) {

            }
            
            if (benimbakiyem < tutar) {
                mesaj = "Yetersiz bakiye. En fazla " + benimbakiyem + " kadar tl çekebilirsiniz.";
            } else {
                try { 
                    Connection conn=connect();
                    String query = ("UPDATE KARTBILGI set BAKIYE = BAKIYE - " + tutar + " WHERE KARTNO='" + KartNo + "'"); 
                       PreparedStatement astm = conn.prepareStatement(query);  
                        astm.executeUpdate(); 
                    conn.close();
                    mesaj =  + tutar + " TL ÇEKME İŞLEMİNİZ BAŞARILI.";

                } catch (Exception e) {

                    System.out.println("Lütfen Gerekli Alanları Doldurunuz");

                }

            }
        return mesaj;
    }
     public String ParaYatirma(Double tutar)
    {
        
        String mesaj = "";
        Preferences pref = Preferences.userNodeForPackage(AnaEkran.class);
        String KartNo = pref.get("kartno", "");
          
            try{
                      Class.forName("org.sqlite.JDBC");
                      Connection conn = this.connect();   
                      String query = ("UPDATE KARTBILGI SET BAKIYE = BAKIYE + " + tutar + " where KARTNO = '" + KartNo + "'");
                       PreparedStatement astm = conn.prepareStatement(query);  
                       astm.executeUpdate();   
                       conn.close(); 
                    mesaj =  + tutar + " TL YATİRMA İŞLEMİNİZ BAŞARILI.";
                } catch (Exception e) {
                    
                    System.out.println(e.getMessage());

                }

            
        return mesaj;
    }
  
     
    public DBConnection() {

    }
}
