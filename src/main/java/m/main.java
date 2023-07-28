package m;

import java.util.Random;

public class main {
    public static void main(String[] args) {
        String content = "**Subscription**:               None (Trial)\n**Visibility Mode**:            Public\n**Fast Time Remaining**:        25.00 minutes\n**Lifetime Usage**:             43 images (0.67 hours)\n**Relaxed Usage**:              0 images (0.00 hours)\n\n**Queued Jobs (fast)**:         0\n**Queued Jobs (relax)**:        0\n**Running Jobs**:               None";
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (!line.startsWith("**Fast Time Remaining**:")) {
                continue;
            }
            String[] times = line.replace("**Fast Time Remaining**:", "").trim().split(" ");
            if (times.length < 2) {
                return;
            }

            String remain = times[0].split("/")[0].trim();
            String unit = times[1].trim();

            float f = Float.parseFloat(remain);
            if (unit.equals("minutes")) {
                System.out.println(Math.round(f * 60));
            } else if (unit.equals("hours")) {
                System.out.println(Math.round(f * 60 * 60));
            }
        }
    }
}