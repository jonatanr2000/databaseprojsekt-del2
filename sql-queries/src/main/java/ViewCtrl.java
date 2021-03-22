import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class ViewCtrl extends DBConn{

    private String email;
    private Integer postId;
    private PreparedStatement regStatement;

    public ViewCtrl() {


    }

    public void view(String email, int postId) {
        this.email = email;
        this.postId = postId;
        try {
            regStatement = conn.prepareStatement("INSERT INTO likes VALUES ( (?), (?) )");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("db error during prepare of insert into Reg");
        }
    }
    public void regLike () {
        if (email != null && postId != null) {
            try {
                regStatement.setString(1, email);
                regStatement.setInt(2, postId);
                regStatement.execute();
            } catch (Exception e) {
                System.out.println("db error during insert of Like user= "+email+" postNr="+postId);
            }
        }
    }

    public void getLike () {
        try {
            PreparedStatement newregStatement = conn.prepareStatement("SELECT * from likes");
            ResultSet jls = newregStatement.executeQuery();

            System.out.println(jls.next());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("db error during insert of Like user= "+email+" postNr="+postId);
        }
    }
/*
    public boolean unLike () {

        // read from runner's Reg into a local copy of loperPoster
        try {
            PreparedStatement loypeStmt = conn.prepareStatement("delete from likes where Email=(?) and Post_Id=(?)");
            loypeStmt.setString(1, user.email);
            loypeStmt.setString(2, user.email);
            ResultSet rs = loypeStmt.executeQuery();
            while (rs.next()) {
                if (startTid == -1) {
                    startTid = rs.getInt("tid");
                }
                sluttTid = rs.getInt("tid");
                loperPoster.add(new Reg(rs.getInt("sekvnr"), rs.getInt("postnr")));
            }

        } catch (Exception e) {
            System.out.println("db error during select of loperposter = "+e);
            return false;
        }


        lopsTid = sluttTid - startTid;

        // read the correct Loype from the database
        int[] loype = new int [100];
        int nPoster = 0;
        try {
            PreparedStatement chkStmt = conn.prepareStatement("select postnr from Loype, Klasse, Loper where Loper.brikkenr= (?) and Loper.klasse=Klasse.klassenavn and Klasse.lnr=Loype.lnr order by Loype.sekvnr");
            chkStmt.setInt(1, brikkeNr);
            ResultSet rs = chkStmt.executeQuery();

            while (rs.next())
            {
                loype[nPoster++]=rs.getInt("postnr");
            }
        } catch (Exception e) {
            System.out.println("db error during select of postnr = "+e);
            return false;
        }

        // check that the runner has done the correct Posts (controls). Set status=dsq in case of wrong controls
        for (int i=0; i<nPoster; i++) {
            if (loype[i] != loperPoster.get(i).reg) {
                try {
                    PreparedStatement updStmt = conn.prepareStatement("update Loper set status='dsq' where brikkenr= (?)");
                    updStmt.setInt(1,brikkeNr);
                    updStmt.execute();
                } catch (Exception e) {
                    System.out.println("db error during update of loper ="+e);
                }
                brikkeNr = INGEN_BRIKKE;
                return false;
            }
        }
        // alt ok
        try {
            PreparedStatement updStmt = conn.prepareStatement("update Loper set status='ok', lopstid=(?) where brikkenr= (?)");
            updStmt.setInt(1, lopsTid);
            updStmt.setInt(2, brikkeNr);
            updStmt.execute();
        } catch (Exception e) {
            System.out.println("db error during update of loper ="+e);
        }
        brikkeNr = INGEN_BRIKKE;
        return true;
    }
*/

    public static void main(String[] args) {

        ViewCtrl viewCtrl = new ViewCtrl();
        viewCtrl.connect();
        viewCtrl.view("ha@gmail.com", 3);
        viewCtrl.regLike();

    }
}

