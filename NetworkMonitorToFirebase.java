import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class NetworkMonitorToFirebase {

    // ◀───  PUT YOUR OWN DETAILS  ───▶
    private static final String DATABASE_URL =
            "https://networkmonitor-aa68b-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String DATABASE_SECRET = "Y6ZqsQbsuEWdXAfKek7QunasYmWdRpIKAmM7yuIF";
    // ────────────────────────────────

    public static void main(String[] args) {

        List<String> targets = Arrays.asList("google.com", "amazon.com", "microsoft.com");

        try {
            InetAddress local = InetAddress.getLocalHost();
            String deviceIP = local.getHostAddress();
            String hostName  = local.getHostName();

            for (String site : targets) {

                InetAddress addr = InetAddress.getByName(site);
                long t0 = System.currentTimeMillis();
                boolean ok = addr.isReachable(3000);
                long ping = System.currentTimeMillis() - t0;

                // Build JSON by hand
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                String json = "{"
                        + "\"deviceIP\":\"" + deviceIP + "\","
                        + "\"hostName\":\"" + hostName + "\","
                        + "\"target\":\""   + site + "\","
                        + "\"targetIP\":\"" + addr.getHostAddress() + "\","
                        + "\"status\":\""   + (ok ? "Reachable" : "Unreachable") + "\","
                        + "\"pingMs\":"     + ping + ","
                        + "\"timestamp\":\"" + timestamp + "\""
                        + "}";

                pushToFirebase(json, site);
            }

            System.out.println("All results pushed to Firebase.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void pushToFirebase(String json, String site) throws Exception {

        URL url = new URL(DATABASE_URL + "pings.json?auth=" + DATABASE_SECRET);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();
        if (code == 200) {
            System.out.println("→ pushed " + site);
        } else {
            System.out.println("Firebase error HTTP " + code);
        }
        conn.disconnect();
    }
}
