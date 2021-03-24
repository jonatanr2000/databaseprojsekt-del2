import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class RegMaalCtrl extends DBConn {

    private static final int INGEN_BRIKKE = -1;
    private int brikkeNr;
    private PreparedStatement regStatement;

    public RegMaalCtrl() {
        brikkeNr = INGEN_BRIKKE;
    }

    public void startReg(int brikkeNr) {
        this.brikkeNr = brikkeNr;
        try {
            regStatement = conn.prepareStatement("INSERT INTO Reg VALUES ( (?), (?), (?), (?))");
        } catch (Exception e) {
            System.out.println("db error during prepare of insert into Reg");
        }
    }

    public void regPost(int sekvNr, int postNr, int tid) {
        if (brikkeNr != INGEN_BRIKKE) {
            try {
                regStatement.setInt(1, sekvNr);
                regStatement.setInt(2, brikkeNr);
                regStatement.setInt(3, postNr);
                regStatement.setInt(4, tid);
                regStatement.execute();
            } catch (Exception e) {
                System.out.println(
                    "db error during insert of Reg sekvnr= " + sekvNr + " postNr=" + postNr);
            }
        }
    }

    public boolean sluttReg() {
        ArrayList<Reg> loperPoster = new ArrayList<Reg>();
        int startTid = -1;
        int sluttTid = -1;
        int lopsTid = -1;

        // read from runner's Reg into a local copy of loperPoster
        try {
            PreparedStatement loypeStmt = conn.prepareStatement(
                "select sekvnr, postnr, tid from Reg where brikkenr=(?) order by sekvnr");
            loypeStmt.setInt(1, brikkeNr);
            ResultSet rs = loypeStmt.executeQuery();
            while (rs.next()) {
                if (startTid == -1) {
                    startTid = rs.getInt("tid");
                }
                sluttTid = rs.getInt("tid");
                loperPoster.add(new Reg(rs.getInt("sekvnr"), rs.getInt("postnr")));
            }

        } catch (Exception e) {
            System.out.println("db error during select of loperposter = " + e);
            return false;
        }


        lopsTid = sluttTid - startTid;

        // read the correct Loype from the database
        int[] loype = new int[100];
        int nPoster = 0;
        try {
            PreparedStatement chkStmt = conn.prepareStatement(
                "select postnr from Loype, Klasse, Loper where Loper.brikkenr= (?) and Loper.klasse=Klasse.klassenavn and Klasse.lnr=Loype.lnr order by Loype.sekvnr");
            chkStmt.setInt(1, brikkeNr);
            ResultSet rs = chkStmt.executeQuery();

            while (rs.next()) {
                loype[nPoster++] = rs.getInt("postnr");
            }
        } catch (Exception e) {
            System.out.println("db error during select of postnr = " + e);
            return false;
        }

        // check that the runner has done the correct Posts (controls). Set status=dsq in case of wrong controls
        for (int i = 0; i < nPoster; i++) {
            if (loype[i] != loperPoster.get(i).reg) {
                try {
                    PreparedStatement updStmt = conn.prepareStatement(
                        "update Loper set status='dsq' where brikkenr= (?)");
                    updStmt.setInt(1, brikkeNr);
                    updStmt.execute();
                } catch (Exception e) {
                    System.out.println("db error during update of loper =" + e);
                }
                brikkeNr = INGEN_BRIKKE;
                return false;
            }
        }
        // alt ok
        try {
            PreparedStatement updStmt = conn.prepareStatement(
                "update Loper set status='ok', lopstid=(?) where brikkenr= (?)");
            updStmt.setInt(1, lopsTid);
            updStmt.setInt(2, brikkeNr);
            updStmt.execute();
        } catch (Exception e) {
            System.out.println("db error during update of loper =" + e);
        }
        brikkeNr = INGEN_BRIKKE;
        return true;
    }

}
