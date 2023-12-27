package ma.fst.toothproject;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV!");
        } else {
            Log.d("OpenCV", "OpenCV loaded Successfully!");
        }
    }

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private TextView anglesTextView;
    private TextView resultTextView;
    private Bitmap selectedBitmap;
    private List<org.opencv.core.Point> selectedPoints = new ArrayList<>();
    private Mat imageMat;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        anglesTextView = findViewById(R.id.anglesTextView);
        resultTextView = findViewById(R.id.resultTextView);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (selectedPoints.size() < 4) {
                    float x = event.getX();
                    float y = event.getY();

                    org.opencv.core.Point openCvPoint = convertToOpenCVPoint(x, y, imageMat.cols(), imageMat.rows());

                    selectedPoints.add(openCvPoint);
                    drawPoint(imageMat, openCvPoint);

                    if (selectedPoints.size() == 4) {
                        double angle1 = calculateAngle(selectedPoints.get(0), selectedPoints.get(1));
                        double angle2 = calculateAngle(selectedPoints.get(1), selectedPoints.get(2));

                        drawLinesBetweenPoints(selectedPoints, imageMat);

                        String anglesText = "<br/><span style='font-weight: bold; color: #766C79;'>Angle 1:</span><br/>" + angle1 +
                                "<br/><span style='font-weight: bold; color: #766C79;'>Angle 2:</span><br/>" + angle2;

                        anglesTextView.setText(Html.fromHtml(anglesText, Html.FROM_HTML_MODE_LEGACY));

                        String resultText;
                        if (angle1 - angle2 > 100) {
                            resultText = "Possible asymétrie";
                        } else {
                            resultText = "Impossible asymétrie";
                        }
                        resultTextView.setText("Résultat : " + resultText);

                        selectedPoints.clear();
                    }
                }
                return false;
            }
        });
    }

    public void choosePhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageView.setVisibility(View.VISIBLE);
            anglesTextView.setVisibility(View.VISIBLE);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                selectedBitmap = bitmap;
                imageView.setImageBitmap(bitmap);

                imageMat = new Mat();
                Utils.bitmapToMat(bitmap, imageMat);

                Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
                Imgproc.GaussianBlur(imageMat, imageMat, new Size(5, 5), 0);
                Imgproc.Canny(imageMat, imageMat, 50, 150);
                Mat dilatedImage = new Mat();
                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
                Imgproc.dilate(imageMat, dilatedImage, kernel);
                Mat lines = new Mat();
                Imgproc.HoughLinesP(dilatedImage, lines, 1, Math.PI / 180, 50, 50, 10);
                Mat contoursImage = new Mat(imageMat.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
                Imgproc.cvtColor(imageMat, contoursImage, Imgproc.COLOR_GRAY2BGR);
                Imgproc.drawContours(contoursImage, getContours(lines), -1, new Scalar(0, 0, 255), 2);
                Bitmap contoursBitmap = Bitmap.createBitmap(contoursImage.cols(), contoursImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(contoursImage, contoursBitmap);

                imageView.setImageBitmap(contoursBitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }

            imageView.post(new Runnable() {
                @Override
                public void run() {
                    Bitmap finalBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(imageMat, finalBitmap);
                    imageView.setImageBitmap(finalBitmap);
                }
            }); 
        }
    }

    private org.opencv.core.Point convertToOpenCVPoint(float touchX, float touchY, int imageWidth, int imageHeight) {
        double openCvX = (touchX / imageView.getWidth()) * imageWidth;
        double openCvY = (touchY / imageView.getHeight()) * imageHeight;
        return new org.opencv.core.Point(openCvX, openCvY);
    }

    private void drawPoint(Mat image, org.opencv.core.Point point) {
        Scalar color = new Scalar(255, 255, 0);
        Imgproc.circle(image, point, 5, color, -1);
        updateImageView();
    }

    private void drawLinesBetweenPoints(List<org.opencv.core.Point> points, Mat image) {
        if (points.size() != 4) {
            Log.e("Point Selection", "Veuillez sélectionner exactement quatre points");
            return;
        }

        Imgproc.line(image, points.get(0), points.get(1), new Scalar(255, 0, 0), 2);
        Imgproc.line(image, points.get(2), points.get(3), new Scalar(255, 0, 0), 2);

        org.opencv.core.Point point0 = points.get(0);
        Imgproc.line(image, new org.opencv.core.Point(point0.x, 0), new org.opencv.core.Point(point0.x, image.rows()), new Scalar(255, 255, 2), 2);

        org.opencv.core.Point point2 = points.get(2);
        Imgproc.line(image, new org.opencv.core.Point(point2.x, 0), new org.opencv.core.Point(point2.x, image.rows()), new Scalar(255, 255, 2), 2);
        updateImageView();
    }

    private void updateImageView() {
        imageView.post(new Runnable() {
            @Override
            public void run() {
                Bitmap finalBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(imageMat, finalBitmap);
                imageView.setImageBitmap(finalBitmap);
            }
        });
    }

    private List<MatOfPoint> getContours(Mat lines) {
        List<MatOfPoint> contours = new ArrayList<>();
        for (int i = 0; i < lines.rows(); i++) {
            double[] l = lines.get(i, 0);
            double x1 = l[0], y1 = l[1], x2 = l[2], y2 = l[3];
            MatOfPoint contour = new MatOfPoint(new org.opencv.core.Point(x1, y1), new org.opencv.core.Point(x2, y2));
            contours.add(contour);
        }
        return contours;
    }

    private double calculateAngle(org.opencv.core.Point point1, org.opencv.core.Point point2) {
        double angleRadians = Math.atan2(point2.y - point1.y, point2.x - point1.x);
        double angleDegrees = Math.toDegrees(angleRadians);
        angleDegrees = (angleDegrees + 360) % 360;
        return angleDegrees;
    }
}