package classification;

public class HoughTransformation {
  private final boolean[][] isEdge;
  private final int acc_threshold;
  private final double acc_accuracy;
  private final int min_major_length;
  private final int max_minor_length; // <= half image size
  private final double acc_bin_size;
  private final double max_b_squared;
  private final ArrayList<ConstPoint> pixels;
  private ArrayList<EllipsisData> results;

  public HoughTransformation(int acc_threshold, double acc_accuracy, int min_major_length, int max_minor_length) {
    this.acc_threshold = acc_threshold;
    this.acc_accuracy = acc_accuracy;
    this.min_major_length = min_major_length;
    this.max_minor_length = max_minor_length;

    this.acc_bin_size = acc_accuracy * acc_accuracy;
    this.max_b_squared = max_minor_length * max_minor_length;
    this.pixels = new ArrayList<>();//edge pixels
  }

  public ArrayList<EllipsisData> findEllipsis(boolean[][] isEdge) {
    // Remove old data
    pixels.clear();
    results = new ArrayList<>();

    // find edge pixels
    for (int a = 0; a < isEdge[0].length; a++) {//TODO check
      for (int b = 0; b < isEdge.length; b++) {
        if (isEdge[b][a]) {
          pixels.add(new ConstPoint(b, a))
        }
      }
    }

    // iterate over all possible combinations of p1 and p2
    for (int p1 = 0; p1 < pixels.size(); ++p1) {
      for (int p2 = 0; p2 < p1; ++p2) {
        internal_stuff(pixels.get(p1), pixels.get(p2))
      }
    }

    pixels.clear(); // To save RAM
    return results;
  }

  private void internal_stuff(ConstPoint p1, ConstPoint p2){
      double xc, yc, a, b, d, k, dx, dy;
      double cos_tau_squared, b_squared, orientation;

      // # Candidate: center (xc, yc) and main axis a
      dx = p1.x - p2.x;
      dy = p1.y - p2.y;
      a = 0.5 * sqrt(dx * dx + dy * dy);
      if (a > 0.5 * min_size){
          ArrayList<Double> accumulator = new ArrayList<>();

          xc = 0.5 * (p1.x + p2.x);
          yc = 0.5 * (p1.y + p2.y);

          for (ConstPoint p3 : pixels) {
              dx = p3.x - xc;
              dy = p3.y - yc;
              d = sqrt(dx * dx + dy * dy);
              if (d > min_size) {
                  dx = p3.x - p1.x;
                  dy = p3.y - p1.y;
                  cos_tau_squared = ((a*a + d*d - dx*dx - dy*dy)
                                     / (2.0 * a * d));
                  cos_tau_squared *= cos_tau_squared;
                  // # Consider b2 > 0 and avoid division by zero
                  k = a*a - d*d * cos_tau_squared;
                  if (k > 0.0 && cos_tau_squared < 1.0) {
                      b_squared = a*a * d*d * (1.0 - cos_tau_squared) / k;
                      // # b2 range is limited to avoid histogram memory
                      // # overflow
                      if (b_squared <= max_b_squared) {
                          accumulator.add(b_squared);
                      }
                  }
              }
          }

          handle_accumulator(accumulator);
        }
      }


        private void handle_accumulator(ArrayList<Double> accumulator){
          if (!accumulator.isEmpty()) {
            // A dynamically sized accumulator

            double max = 0;
            for (double d : accumulator){
              max = Math.max(max, d);
            }

            int bin_count = Math.ceil(max / acc_bin_size);
            int[] bins = new int[bin_count];

            for (double d : accumulator){
              int index = (int) (d / bin_size);
              bins[index] += 1;
            }

            double bin_max_center = 0, bin_max_count = 0;
            for (int i = 0; i < bins.length; ++i) {
              if (bins[i] > bin_max_count) {
                bin_max_count = bins[i];
                bin_max_center = i;// max index
              }
            }

            bin_max_center = (i + 0.5) * bin_size; // convert index to bin center

            if (bin_max_count > acc_threshold) {
                orientation = Math.atan2(p1.x - p2.x, p1.y - p2.y);
                b = bin_max_center;

                //Start TODO do we need this?
                // # to keep ellipse_perimeter() convention
                if (orientation != 0) {
                    orientation = Math.PI - orientation
                    // # When orientation is not in [-pi:pi]
                    // # it would mean in ellipse_perimeter()
                    // # that a < b. But we keep a > b.
                    if (orientation > Math.PI){
                        orientation = orientation - Math.PI / 2.0.
                        double tmp = a;
                        a = b;
                        b = tmp;
                      }
                }
                //end
                results.add(new EllipsisData(xc, yc, a, b, bin_max_count, orientation));
              }
          }
        }

  public static final class EllipsisData {
    private final int xc, yc;
    private final double a, b, accumulator, orientation;

    private EllipsisData(int xc, int yc, double a, double b, double accumulator, double orientation) {
      this.xc = xc;
      this.yc = yc;
      this.a = a;
      this.b = b;
      this.accumulator = accumulator;
      this.orientation = orientation;
    }
  }

  private static final class ConstPoint {
    private final int x, y;

    private ConstPoint(int x, int y){
      this.x = x;
      this.y = y;
    }
  }
}
