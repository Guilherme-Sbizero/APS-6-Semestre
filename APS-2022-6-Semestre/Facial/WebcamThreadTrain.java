package com.source.control;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_face.EigenFaceRecognizer;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.FisherFaceRecognizer;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import com.source.model.Imag;
import com.view.controllers.CMainFrame;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class WebcamThreadTrain extends Task<Void>{

	private ImageView view;
	private VideoCapture cap;
	private FisherRecog recog;
	private List<Imag> faceFrames = new ArrayList<Imag>();
	private Integer label;
	private String nome;
	private Integer reduceArray = 4;

	public WebcamThreadTrain(ImageView view, VideoCapture cap,
			FisherRecog recog,Integer label, String nome) {
		this.view = view;
		this.cap = cap;
		this.recog = recog;
		this.label = label;
		this.nome = nome;
	}

	@Override
	protected Void call()  {
		try {
			Imag imgFace = new Imag(label,nome, null, new Mat(), false, new RectVector(), new Rect());
			System.out.println(label);
			while(!cap.isNull() && cap.isOpened() && view.isVisible()) {
				System.out.println(cap.read(imgFace.getImagem()));
				
				
				imgFace.setRostos(Utilitarios.detectFaces(recog.getCascadeClassifier(), imgFace.getImagem()));
				if (imgFace.getRostos().size() > 0) {
					
					imgFace.setRostoPrinc(Utilitarios.detectFacePrincipal(imgFace.getRostos()));
					if(imgFace.getRostoPrinc().width() >= FisherRecog.resizeRows && imgFace.getRostoPrinc().height() >= FisherRecog.resizeColumn ) {
						System.out.println("Input image rows " + imgFace.getImagem().rows());
						System.out.println("input channels " + imgFace.getImagem().channels());
						faceFrames.add(new Imag(label, nome, null, imgFace.getImagem(), false, imgFace.getRostos(), imgFace.getRostoPrinc()));
						imgFace.setImagem(drawBlueRec(imgFace.getImagem(), imgFace.getRostoPrinc()));
					}else {
						imgFace.setImagem(drawRedRec(imgFace.getImagem(), imgFace.getRostoPrinc()));
						
					}
					System.out.println(imgFace.getImagem().rows() + " teeeee");
					System.out.println(imgFace.getImagem().channels() + " teeeee ch");
					
	                /*opencv_imgproc.putText(imgFace.getImagem(), CMainFrame.txt, new Point(Math.max(imgFace.getRostoPrinc().tl().x() - 10, 0),
	                		Math.max(imgFace.getRostoPrinc().tl().y() - 10, 0)),
	                		opencv_imgproc.FONT_HERSHEY_PLAIN, 5.0, new Scalar(255, 0, 255, 2.0),3,
	                		opencv_imgproc.LINE_4,false);*/
					view.setImage(Utilitarios.convertMatToImage(imgFace.getImagem()));
					
					
				}else {
					System.out.println(imgFace.getImagem().rows());
					view.setImage(Utilitarios.convertMatToImage(imgFace.getImagem()));
				}
				Thread.sleep(1);
			}
			reduceListSize(faceFrames);
			
			Platform.runLater(()->{

				try {
					recog.addImagensTrain(faceFrames);
					imgFace.close();
					imgFace.close();
					cap.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			});
			

			view.setImage(null);
			return null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	private Mat drawRedRec(Mat frame, Rect rect) throws Exception{
		Mat img = new Mat(frame);
		opencv_imgproc.rectangle(img, new Point(rect.x(), rect.y()),
				new Point(rect.x() + rect.width(), rect.y() + rect.height()), Scalar.RED, 1, opencv_imgproc.LINE_AA, 0);
		return img;
	}
	private Mat drawBlueRec(Mat frame, Rect rect) throws Exception{
		Mat img = new Mat(frame);
		opencv_imgproc.rectangle(img, new Point(rect.x(), rect.y()),
				new Point(rect.x() + rect.width(), rect.y() + rect.height()), Scalar.BLUE, 1, opencv_imgproc.LINE_AA, 0);
		return img;
	}
	
	private void reduceListSize(List<Imag> faceFrames){
		int counter = 0;
		for(int i = 0;i<faceFrames.size();i++) {
			if(reduceArray == counter) {
				faceFrames.remove(i);
				counter = 0;
			}else {
				counter++;
			}
		}
		
		
	}
}