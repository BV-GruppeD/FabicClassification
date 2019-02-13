package classification;

public class HoughTransformation {
  private final boolean[][] isEdge;
  private final int acc_threshold;
  private final double acc_accuracy;
  private final int min_major_length;
  private final int max_minor_length; // < half image size

  private void run() {
    int[]?? pixelcount = ???;
    double acc_bin_size = acc_accuracy * acc_accuracy;
    double max_b_squared = max_minor_length * max_minor_length;

    ArrayList<ConstPoint> pixels = new ArrayList<>();//edge pixels
    for (int a = 0; a < isEdge[0].length; a++) {//TODO check
      for (int b = 0; b < isEdge.length; b++) {
        if (isEdge[b][a]) {
          pixels.add(new ConstPoint(b, a))
        }
      }
    }

    int p1, p2, p3, p1x, p1y, p2x, p2y, p3x, p3y;
      double xc, yc, a, b, d, k, dx, dy;
      double cos_tau_squared, b_squared, orientation;

      for (ConstPoint p1 : pixels) {

      }

      for p1 in range(num_pixels):
          p1x = pixels[1, p1]
          p1y = pixels[0, p1]

          for p2 in range(p1):
              p2x = pixels[1, p2]
              p2y = pixels[0, p2]

              # Candidate: center (xc, yc) and main axis a
              dx = p1x - p2x
              dy = p1y - p2y
              a = 0.5 * sqrt(dx * dx + dy * dy)
              if a > 0.5 * min_size:
                  xc = 0.5 * (p1x + p2x)
                  yc = 0.5 * (p1y + p2y)

                  for p3 in range(num_pixels):
                      p3x = pixels[1, p3]
                      p3y = pixels[0, p3]
                      dx = p3x - xc
                      dy = p3y - yc
                      d = sqrt(dx * dx + dy * dy)
                      if d > min_size:
                          dx = p3x - p1x
                          dy = p3y - p1y
                          cos_tau_squared = ((a*a + d*d - dx*dx - dy*dy)
                                             / (2 * a * d))
                          cos_tau_squared *= cos_tau_squared
                          # Consider b2 > 0 and avoid division by zero
                          k = a*a - d*d * cos_tau_squared
                          if k > 0 and cos_tau_squared < 1:
                              b_squared = a*a * d*d * (1 - cos_tau_squared) / k
                              # b2 range is limited to avoid histogram memory
                              # overflow
                              if b_squared <= max_b_squared:
                                  acc.append(b_squared)

                  if len(acc) > 0:
                      bins = np.arange(0, np.max(acc) + bin_size, bin_size)
                      hist, bin_edges = np.histogram(acc, bins=bins)
                      hist_max = np.max(hist)
                      if hist_max > threshold:
                          orientation = atan2(p1x - p2x, p1y - p2y)
                          b = sqrt(bin_edges[hist.argmax()])
                          # to keep ellipse_perimeter() convention
                          if orientation != 0:
                              orientation = M_PI - orientation
                              # When orientation is not in [-pi:pi]
                              # it would mean in ellipse_perimeter()
                              # that a < b. But we keep a > b.
                              if orientation > M_PI:
                                  orientation = orientation - M_PI / 2.
                                  a, b = b, a
                          results.append((hist_max, # Accumulator
                                          yc, xc,
                                          a, b,
                                          orientation))
                      acc = []

      return np.array(results, dtype=[('accumulator', np.intp),
                                      ('yc', np.double),
                                      ('xc', np.double),
                                      ('a', np.double),
                                      ('b', np.double),
  ('orientation', np.double)])
  }

  private static final class ConstPoint {
    private final int x, y;

    private ConstPoint(int x, int y){
      this.x = x;
      this.y = y;
    }
  }
}
