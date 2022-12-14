package com.source.control;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import com.source.model.Imag;
import com.sun.glass.ui.View;

import javafx.concurrent.Task;
import javafx.scene.image.ImageView;

public class WebcamThreadDetect extends Task<Void> {

	private ImageView view;
	private VideoCapture cap;
	private CascadeClassifier cas;
	private FisherRecog recog;
	private double thres = 300;

	public WebcamThreadDetect(ImageView view, FisherRecog recog) {
		this.view = view;
		this.cas = recog.getCascadeClassifier();
		this.cap = new VideoCapture(0);
		this.recog = recog;
	}

	@Override
	protected Void call() {
		try {
			recog.setThreshold(thres);
			Imag imgFace = new Imag(1, null,null, new Mat(), false, new RectVector(), new Rect());
			Mat frame = new Mat();
			while (!cap.isNull() && cap.isOpened() && view.isVisible()) {
				System.out.println(imgFace.isProces() + " process");
				System.out.println(cap.read(imgFace.getImagem()));
				frame = imgFace.getImagem().clone();
				
				imgFace.setProces(false);
				imgFace.setRostos(Utilitarios.detectFaces(cas, imgFace.getImagem()));
				
				if (imgFace.getRostos().size() > 0) {
					imgFace.setRostoPrinc(Utilitarios.detectFacePrincipal(imgFace.getRostos()));
					
					if (imgFace.getRostoPrinc().width() >= 150 && imgFace.getRostoPrinc().height() >= 150) {
						
						imgFace.setImagem(recog.processImage(imgFace.getImagem(), imgFace.getRostoPrinc()));
						imgFace.setProces(true);
						int[] labelPrece = recog.identificarRosto(imgFace);
						imgFace.setImagem(drawBlueRec(frame, imgFace.getRostoPrinc()));
						if (labelPrece[0] > -1) {
							opencv_imgproc.putText(imgFace.getImagem(), recog.getLabelInfo(labelPrece[0]),
									new Point(imgFace.getRostoPrinc().x(), imgFace.getRostoPrinc().y()),
									opencv_imgproc.FONT_HERSHEY_PLAIN, 3, new Scalar(0, 255, 0, 2.0), 3,
									opencv_imgproc.LINE_AA, false);
							opencv_imgproc.putText(imgFace.getImagem(), "% " + calcularPorcentagem(labelPrece[1]),
									new Point(imgFace.getRostoPrinc().x(),
											imgFace.getRostoPrinc().y() + imgFace.getRostoPrinc().height() + 10),
									opencv_imgproc.FONT_HERSHEY_PLAIN, 3, new Scalar(0, 255, 0, 2.0), 3,
									opencv_imgproc.LINE_AA, false);
						}
					} else {
						imgFace.setImagem(drawRedRec(imgFace.getImagem(), imgFace.getRostoPrinc()));

					}

				}
				view.setImage(Utilitarios.convertMatToImage(imgFace.getImagem()));
				Thread.sleep(1);
			}

			imgFace.close();
			cap.close();
			frame.close();
			view.setImage(null);
			return null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Mat drawRedRec(Mat frame, Rect rect) throws Exception {
		Mat img = frame.clone();
		opencv_imgproc.rectangle(img, new Point(rect.x(), rect.y()),
				new Point(rect.x() + rect.width(), rect.y() + rect.height()), Scalar.RED, 1, opencv_imgproc.LINE_AA, 0);
		return img;
	}

	private Mat drawBlueRec(Mat frame, Rect rect) throws Exception {
		Mat img = frame.clone();
		opencv_imgproc.rectangle(img, new Point(rect.x(), rect.y()),
				new Point(rect.x() + rect.width(), rect.y() + rect.height()), Scalar.BLUE, 1, opencv_imgproc.LINE_AA,
				0);
		return img;
	}

	private int calcularPorcentagem(int result) {
		result = (int) (((result * 100)/thres) - 100) * -1 ;
		return result;
	}
}