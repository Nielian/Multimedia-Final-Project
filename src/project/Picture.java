package project;

import ij.ImagePlus;

import java.net.URL;
import java.util.ResourceBundle;

import Jama.Matrix;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;

// tøída pro ovládání prvkù GUI, využívá funkcí PictureTransform
// ve srovnání s PictureTransform je to "vyšší vrstva"

public class Picture implements Initializable {

	private ImagePlus imagePlus1;
	private ImagePlus imagePlus2;

	private PictureTransform I_Orig;
	private PictureTransform I_Copy;
	private PictureTransform P_Orig;
	private PictureTransform P_Estimated;

	public static final int RED = 1;
	public static final int GREEN = 2;
	public static final int BLUE = 3;
	public static final int Y = 4;
	public static final int CB = 5;
	public static final int CR = 6;
	public static final int S444 = 7;
	public static final int S422 = 8;
	public static final int S420 = 9;
	public static final int S411 = 10;
	private int vzorkovani = S444;
	private int blockSize = 8; // defaultni nastaveni pro DCT

	private Motion motion;
	
	@FXML
	private Button redButton;
	@FXML
	private Button greenButton;
	@FXML
	private Button blueButton;
	@FXML
	private Button yButton;
	@FXML
	private Button cbButton;
	@FXML
	private Button crButton;
	@FXML
	private Slider slider;
	
	@Override
 	public void initialize(URL arg0, ResourceBundle arg1) {
		// vola se i bez konstruktoru pri vytvoreni objektu
		nactiObrazyZeSouboru(); // vytvori RGB matice v PictureTransform

		// vytvoreni YCbCr matic
		I_Orig.convertRgbToYcbcr();
		I_Copy.convertRgbToYcbcr();
		P_Orig.convertRgbToYcbcr();
		P_Estimated.convertRgbToYcbcr();

		// P_Estimated rovnou rozsirime o 8 px na kazdou stranu pro ucel odhadu pohybu
		Matrix wideP = new Matrix( P_Orig.getY().getArray() );
		wideP = P_Orig.wideMat(wideP);
		wideP = wideP.transpose();
		wideP = P_Orig.wideMat(wideP);
		wideP = wideP.transpose();
		P_Estimated.setY(wideP);
		
//		Matrix wideP_Cb = new Matrix( P_Orig.getcB().getArray() );
//		wideP_Cb = P_Orig.wideMat(wideP_Cb);
//		wideP_Cb = wideP_Cb.transpose();
//		wideP_Cb = P_Orig.wideMat(wideP_Cb);
//		wideP_Cb = wideP_Cb.transpose();
//		P_Estimated.setcB(wideP_Cb);
//		
//		Matrix wideP_Cr = new Matrix( P_Orig.getcR().getArray() );
//		wideP_Cr = P_Orig.wideMat(wideP_Cr);
//		wideP_Cr = wideP_Cr.transpose();
//		wideP_Cr = P_Orig.wideMat(wideP_Cr);
//		wideP_Cr = wideP_Cr.transpose();
//		P_Estimated.setcR(wideP_Cr);
		
		// zobrazeni originalniho obrazu pri spusteni programu
		imagePlus1.setTitle("Picture 1");
		imagePlus1.show("Picture 1");
		imagePlus2.setTitle("Picture 2");
		imagePlus2.show("Picture 2");
		
		motion = new Motion();
	}

	private void nactiObrazyZeSouboru() {
		this.imagePlus1 = new ImagePlus("pomaly.jpg");
		this.imagePlus2 = new ImagePlus("pomaly2.jpg");

		I_Orig = new PictureTransform(imagePlus1.getBufferedImage());
		/*
		 * zavola se konstruktor pictureTransform a vytvori se matice Y,Cb a Cr
		 * vhodne velikosti a matice r,g a b vhodne velikosti, vsechny zatim
		 * prazdne
		 */
		I_Orig.getRGB();

		I_Copy = new PictureTransform(imagePlus1.getBufferedImage());
		I_Copy.getRGB();

		// nyni se vytvori zobrazitelne matice RGB v PictureTransform2
		P_Orig = new PictureTransform(imagePlus2.getBufferedImage());
		P_Orig.getRGB();
		
		P_Estimated = new PictureTransform(imagePlus2.getBufferedImage());
		P_Estimated.getRGB();
	}

	public ImagePlus getComponent(int component) {
		ImagePlus imagePlus = null;
		switch (component) {
		case RED:
			imagePlus = I_Orig.setImageFromRGB(
					I_Orig.getImageWidth(),
					I_Orig.getImageHeight(),
					I_Orig.getRed(), "RED");
			break;
		// podobnì vytvoøte case pro GREEN a BLUE
		case GREEN:
			imagePlus = I_Orig.setImageFromRGB(
					I_Orig.getImageWidth(),
					I_Orig.getImageHeight(),
					I_Orig.getGreen(), "GREEN");
			break;
		case BLUE:
			imagePlus = I_Orig.setImageFromRGB(
					I_Orig.getImageWidth(),
					I_Orig.getImageHeight(),
					I_Orig.getBlue(), "BLUE");
			break;
		case Y:
			imagePlus = I_Orig.setImageFromRGB(I_Orig
					.getY().getColumnDimension(), I_Orig.getY()
					.getRowDimension(), I_Orig.getY(), "Y");
			break;
		case CB:
			imagePlus = I_Orig.setImageFromRGB(I_Orig
					.getcB().getColumnDimension(), I_Orig.getcB()
					.getRowDimension(), I_Orig.getcB(), "Cb");
			break;
		case CR:
			imagePlus = I_Orig.setImageFromRGB(I_Orig
					.getcR().getColumnDimension(), I_Orig.getcR()
					.getRowDimension(), I_Orig.getcR(), "Cr");
		default:
			break;
		}
		return imagePlus;
	}

	public void downsample(int downsampleType) {
		// I_Orig.convertRgbToYcbcr();
		Matrix cB = new Matrix( I_Orig.getcB().getArray() );
		Matrix cR = new Matrix( I_Orig.getcR().getArray() );
		
		switch (downsampleType) {
		case S444:
			cB = I_Copy.getcB();
			I_Copy.setcB(cB);

			cR = I_Copy.getcR();
			I_Copy.setcR(cR);
			break;
		case S422:
			cB = I_Orig.downsample(cB);
			I_Orig.setcB(cB);

			cR = I_Orig.downsample(cR);
			I_Orig.setcR(cR);
			break;

		case S420:
			cB = I_Orig.downsample(cB);
			cB = cB.transpose();
			cB = I_Orig.downsample(cB);
			cB = cB.transpose();
			I_Orig.setcB(cB);

			cR = I_Orig.downsample(cR);
			cR = cR.transpose();
			cR = I_Orig.downsample(cR);
			cR = cR.transpose();
			I_Orig.setcR(cR);
			break;

		case S411:
			cB = I_Orig.downsample(cB);
			I_Orig.setcB(cB);
			cB = new Matrix(I_Orig.getcB().getArray());
			cB = I_Orig.downsample(cB);
			I_Orig.setcB(cB);

			cR = I_Orig.downsample(cR);
			I_Orig.setcR(cR);
			cR = new Matrix(I_Orig.getcR().getArray());
			cR = I_Orig.downsample(cR);
			I_Orig.setcR(cR);
			break;
		}
	}

	public void oversample(int oversample) {
		Matrix cB;
		Matrix cR;
		switch (oversample) {
		case S444:
			I_Orig.setcB( I_Copy.getcB() );
			I_Orig.setcR( I_Copy.getcR() );
			break;
			
		case S422:
			cB = new Matrix(I_Orig.getcB().getArray());
			cB = I_Orig.oversample(cB);
			I_Orig.setcB(cB);
			
			cR = new Matrix(I_Orig.getcR().getArray());
			cR = I_Orig.oversample(cR);
			I_Orig.setcR(cR);
			break;

		case S420:
			cB = new Matrix(I_Orig.getcB().getArray()).transpose();
			cB = I_Orig.oversample(cB);
			I_Orig.setcB(cB);
			cB = new Matrix(I_Orig.getcB().getArray()).transpose();
			cB = I_Orig.oversample(cB);
			I_Orig.setcB(cB);

			cR = new Matrix(I_Orig.getcR().getArray()).transpose();
			cR = I_Orig.oversample(cR);
			I_Orig.setcR(cR);
			cR = new Matrix(I_Orig.getcR().getArray()).transpose();
			cR = I_Orig.oversample(cR);
			I_Orig.setcR(cR);
			break;

		case S411:
			cB = new Matrix(I_Orig.getcB().getArray());
			cB = I_Orig.oversample(cB);
			I_Orig.setcB(cB);
			cB = new Matrix(I_Orig.getcB().getArray());
			cB = I_Orig.oversample(cB);
			I_Orig.setcB(cB);

			cR = new Matrix(I_Orig.getcR().getArray());
			cR = I_Orig.oversample(cR);
			I_Orig.setcR(cR);
			cR = new Matrix(I_Orig.getcR().getArray());
			cR = I_Orig.oversample(cR);
			I_Orig.setcR(cR);
			break;

		default:
			break;
		}
		I_Orig.convertYcbcrToRgb();
	}

	public void dpcm(ActionEvent event) {
		Matrix dY = new Matrix(P_Estimated.getImageHeight(), P_Estimated.getImageWidth());
//		Matrix dcB = new Matrix(P_Estimated.getImageHeight(), P_Estimated.getImageWidth());
//		Matrix dcR = new Matrix(P_Estimated.getImageHeight(), P_Estimated.getImageWidth());

		Matrix wideP_Orig = new Matrix( P_Orig.getY().getArray() );
		wideP_Orig = P_Orig.wideMat(wideP_Orig);
		wideP_Orig = wideP_Orig.transpose();
		wideP_Orig = P_Orig.wideMat(wideP_Orig);
		wideP_Orig = wideP_Orig.transpose();
		
		dY = P_Estimated.getY().minus(wideP_Orig);
		P_Orig.setdY(dY);
		P_Estimated.setdY(dY);

//		Matrix wideP_OrigCb = new Matrix( P_Orig.getcB().getArray() );
//		wideP_OrigCb = P_Orig.wideMat(wideP_OrigCb);
//		wideP_OrigCb = wideP_OrigCb.transpose();
//		wideP_OrigCb = P_Orig.wideMat(wideP_OrigCb);
//		wideP_OrigCb = wideP_OrigCb.transpose();
//		
//		dcB = P_Estimated.getcB().minus(wideP_OrigCb);
//		P_Orig.setDcB(dcB);
//		P_Estimated.setDcB(dcB);
//
//		Matrix wideP_OrigCr = new Matrix( P_Orig.getcR().getArray() );
//		wideP_OrigCr = P_Orig.wideMat(wideP_OrigCr);
//		wideP_OrigCr = wideP_OrigCr.transpose();
//		wideP_OrigCr = P_Orig.wideMat(wideP_OrigCr);
//		wideP_OrigCr = wideP_OrigCr.transpose();
//		
//		dcR = P_Estimated.getcR().minus(wideP_OrigCr);
//		P_Orig.setDcR(dcR);
//		P_Estimated.setDcR(dcR);

		// pro zobrazeni - pouze jasova slozka
		for (int i = 0; i < dY.getRowDimension(); i++) {
			for (int j = 0; j < dY.getColumnDimension(); j++) {
				dY.set(i, j, (dY.get(i, j) + 255) * 0.5);
			}
		}
		// vytvoreni a zobrazeni obrazku s touto komponentou
		P_Estimated.setImageFromRGB(dY.getRowDimension(), dY.getColumnDimension(), dY, "dY").show();
	}

	// Full search
	public void fullSearch(ActionEvent event) {
		motion.setWideP_applyMotionVectors(P_Estimated.getY());
		motion.setWideP(P_Estimated.getY());
		int blockIndex = 0;

		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 15; j++) {
				motion.fullSearchEstimation(blockIndex, P_Estimated.getY().getMatrix(i*16, i*16+31, j*16, j*16+31), I_Orig.getY().getMatrix(i*16, i*16+15, j*16, j*16+15));
				++blockIndex;
			}
		}		
		P_Estimated.setY( motion.getWideP_applyMotionVectors() );
	}

	public void oneAtTimeSearch(ActionEvent event) {
		motion.setWideP_applyMotionVectors(P_Estimated.getY());
		motion.setWideP(P_Estimated.getY());

		int blockIndex = 0;
		
		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 15; j++) {
				motion.oneAtTimeSearchEstimation(blockIndex, i, j, I_Orig.getY().getMatrix(i*16, i*16+15, j*16, j*16+15));
//				System.out.println(blockIndex);
				++blockIndex;
			}
		}
		P_Estimated.setY( motion.getWideP_applyMotionVectors() );
	}
	
	public void rButtonPressed(ActionEvent event) {
		getComponent(Picture.RED).show("Red Component");
	}

	public void gButtonPressed(ActionEvent event) {
		getComponent(Picture.GREEN).show("Green Component");
	}

	public void bButtonPressed(ActionEvent event) {
		getComponent(Picture.BLUE).show("Blue Component");
	}

	public void yButtonPressed(ActionEvent event) {
		getComponent(Picture.Y).show("Y Component");
	}

	public void cbButtonPressed(ActionEvent event) {
		getComponent(Picture.CB).show("Cb Component");
	}

	public void crButtonPressed(ActionEvent event) {
		getComponent(Picture.CR).show("Cr Component");
	}

	public void dS444ButtonPressed(ActionEvent event) {
		vzorkovani = S444;
		downsample(S444);
	}

	public void dS422ButtonPressed(ActionEvent event) {
		vzorkovani = S422;
		downsample(S422);
	}

	public void dS420ButtonPressed(ActionEvent event) {
		vzorkovani = S420;
		downsample(S420);
	}

	public void dS411ButtonPressed(ActionEvent event) {
		vzorkovani = S411;
		downsample(S411);
	}

	public void overSampleButtonPressed(ActionEvent event) {
		oversample(vzorkovani);
	}

	public void showResult() {
		I_Orig.convertYcbcrToRgb();
		I_Orig.setImageFromRGB(I_Orig.getRed().length,
				I_Orig.getRed()[0].length,
				I_Orig.getRed(), I_Orig.getGreen(),
				I_Orig.getBlue()).show("Transformed Image");
	}

	public void dctTransform(ActionEvent event) {
		Matrix yPom = new Matrix(I_Orig.getImageHeight(), I_Orig.getImageWidth());
		Matrix cbPom = new Matrix(I_Orig.getImageHeight(), I_Orig.getImageWidth());
		Matrix crPom = new Matrix(I_Orig.getImageHeight(), I_Orig.getImageWidth());
		
		yPom = I_Orig.blockTransformation(I_Orig.getY(), blockSize, (int) slider.getValue(), true);
		cbPom = I_Orig.blockTransformation(I_Orig.getcB(), blockSize, (int) slider.getValue(), false);
		crPom = I_Orig.blockTransformation(I_Orig.getcR(), blockSize, (int) slider.getValue(), false);
		
		I_Orig.setY(yPom);
		I_Orig.setcB(cbPom);
		I_Orig.setcR(crPom);
	}

	public void idctTransform(ActionEvent event) {
		Matrix yPom = new Matrix(I_Orig.getImageHeight(), I_Orig.getImageWidth());
		Matrix cbPom = new Matrix(I_Orig.getImageHeight(), I_Orig.getImageWidth());
		Matrix crPom = new Matrix(I_Orig.getImageHeight(), I_Orig.getImageWidth());
		
		yPom = I_Orig.inverseBlockTransformation(I_Orig.getY(), blockSize, (int) slider.getValue(),true);
		cbPom = I_Orig.inverseBlockTransformation(I_Orig.getcB(), blockSize, (int) slider.getValue(),false);
		crPom = I_Orig.inverseBlockTransformation(I_Orig.getcR(), blockSize, (int) slider.getValue(),false);
		
		I_Orig.setY(yPom);
		I_Orig.setcB(cbPom);
		I_Orig.setcR(crPom);
	}

	public void b2ButtonPressed(ActionEvent event) {
		blockSize = 2;
	}

	public void b4ButtonPressed(ActionEvent event) {
		blockSize = 4;
	}

	public void b8ButtonPressed(ActionEvent event) {
		blockSize = 8;
	}
	
	public void ypButtonPressed(ActionEvent event) {
		// odhadnuty snimek P pomoci Full Search nebo One-At-Time Search
		P_Estimated.setImageFromRGB(P_Estimated.getImageWidth(), P_Estimated.getImageHeight(), P_Estimated.getY(), "Y").show();
	}
}
