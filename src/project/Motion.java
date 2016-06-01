package project;

import Jama.Matrix;

// trida urcena k odhadum pohybu
public class Motion {

	private Matrix motionVectorsFS;
	private Matrix motionVectorsOATS;
	private Matrix wideP_applyMotionVectors;
	private Matrix wideP;

	public Motion() {
		motionVectorsFS = new Matrix(225, 2);
		motionVectorsOATS = new Matrix(225, 2);
		wideP_applyMotionVectors = new Matrix(256, 256);
		wideP = new Matrix(256, 256);
	}

	/*
	 * FULLSEARCH Mame oblast vyhledavani ve snimku I a makroblok ze snimku P.
	 * Hledame nejpodobnejsi makroblok ve snimku I kdyz ho najdeme, do vektoru
	 * pohybu zapiseme ktery makroblok to je (1.,2.,3. ...) a o kolik se
	 * posunul. Kombinuje se s funkci ve tride PictureTransform
	 */
	public void fullSearchEstimation(int blockNum, Matrix searchArea, Matrix macroBlock) {
		// searchArea 32x32, macroBlock 16x16 ve støedu SearchArea
		// vytvoreni vektoru pohybu
		double sae;
		double minSae = 100000.00;

//		System.out.print("Novy blok cislo "+blockNum+" mv: ");
		
		for (int i = 0; i <= 16; i++) {
			for (int j = 0; j <= 16; j++) {
				sae = this.SAE( searchArea.getMatrix(i, 15 + i, j, 15 + j), macroBlock );
//				searchArea.getMatrix(i, 15 + i, j, 15 + j).print(2, 2);
//				macroBlock.print(2, 2);

				if (sae < minSae) {
					minSae = sae;
					// blockNum = radek, 0 a 1 sloupec
					motionVectorsFS.set(blockNum, 0, j - 8);
					motionVectorsFS.set(blockNum, 1, i - 8);
				}

			}
		}
		// zaroven se hned provede kompenzace
		compensation(getMotionVectorsFS());
//		System.out.print( motionVectorsFS.get(blockNum, 0)+" ");
//		System.out.println( motionVectorsFS.get(blockNum, 1) );
	}

	public void oneAtTimeSearchEstimation(int blockNum, int m, int n, Matrix macroBlockFromI) {
		double sae[] = new double[3];
		boolean horizontal = true;
		// sae[0] vlevo/dole, sae[1] uprostred, sae[2] vpravo/nahore
		
		for (int p = -1; p <= 1; p++){ 
			sae[1 + p] = SAE( wideP.getMatrix(m * 16 + 8 + p, m * 16 + 8 + 15 + p, n * 16 + 8, n * 16 + 8 + 15) , macroBlockFromI);
		}

		int i = 2; int j = 2; int k = 2; int l = 2; // pocitadla

		while (true) {
			// SAE prostredniho snimku je mensi/roven sousednim
			if ( (sae[1] <= sae[0]) && (sae[1] <= sae[2]) ) { 
				if (horizontal == true){ // pokud nyni probihalo hledani horizontalne
					horizontal = false; // nasleduje vertikalne
					for (int p = -1; p <= 1; p++){ 
						sae[1 + p] = SAE(wideP.getMatrix(m * 16 + 8, m * 16 + 8 + 15, n * 16 + 8 + p, n * 16 + 8 + 15 + p), macroBlockFromI);
					}
				}
				else
					break; // uz probehlo horizontalni i vertikalni => konec
			}
			// posun vlevo nebo dolu, pokud se rovnaji, tento smer ma prednost
			else if ( sae[0] <= sae[2] ) { 
				sae[2] = sae[1];
				sae[1] = sae[0];

				if (horizontal == true) {
					// try catch pro prevenci kraje nezabralo
					if( i < 10){
						sae[0] = SAE(wideP.getMatrix(m * 16 + 8 - i, m * 16 + 8 + 15 - i, n * 16 + 8, n * 16 + 8 + 15), macroBlockFromI);
						++i;
						--j;
					} else {
						// pokud dojde k chybe, predpokladam, ze narazil na stenu, v tom pripade vyberu blok u steny
						horizontal = false;
						sae[0] = 10000.00; // zajistim aby pokracovalo vertikalni hledani
						continue;
					}
				} else {
					if( m < 10 ){
						sae[0] = SAE(wideP.getMatrix(m * 16 + 8, m * 16 + 8 + 15, n * 16 + 8 - k, n * 16 + 8 + 15 - k), macroBlockFromI);
						++k;
						--l;
					} else {
						// pokud dojde k chybe, predpokladam, ze narazil na stenu, v tom pripade vyberu blok u steny 
						break; // konec hledani
					}
				}
				continue;
			}
			// posun vpravo nebo nahoru
			else if ( sae[2] < sae[0] ) { 
				sae[0] = sae[1];
				sae[1] = sae[2];
				
				if(horizontal == true){
					if( n < 10) {
						sae[2] = SAE(wideP.getMatrix(m * 16 + 8 + j, m * 16 + 8 + 15 + j, n * 16 + 8, n * 16 + 8 + 15), macroBlockFromI);
						++j;
						--i;
					} else {
						horizontal = false;
						sae[2] = 10000.00; // zajistim aby pokracovalo vertikalni hledani
						continue;
					}
					
				} else {
					if( l < 10) {
						sae[2] = SAE(wideP.getMatrix(m * 16 + 8, m * 16 + 8 + 15, n * 16 + 8 + l, n * 16 + 8 + 15 + l), macroBlockFromI);
						++l;
						--k;
					} else {
						break;
					}
					
				}
				continue;
			}
		}
		motionVectorsOATS.set(blockNum, 0, j-2);
		motionVectorsOATS.set(blockNum, 1, k-2);
		System.out.print( motionVectorsOATS.get(blockNum, 0)+" ");
		System.out.println( motionVectorsOATS.get(blockNum, 1) );
		compensation(motionVectorsOATS);
	}

	public void compensation(Matrix motionVectors) {
		// aplikace vektoru pohybu, pracujeme s rozsirenym snimkem P
		// nejprve pouze 1 blok
		int blockNum = 0;

		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 15; j++) {
				wideP_applyMotionVectors.setMatrix(
						(int) (i*16+8+motionVectors.get(blockNum, 0)), (int) (i*16+8+15+motionVectors.get(blockNum, 0)), 
						(int) (j*16+8+motionVectors.get(blockNum, 1)), (int) (j*16+8+15+motionVectors.get(blockNum, 1)), 
						wideP.getMatrix(i*16+8, i*16+15+8, j*16+8, j*16+15+8));
				++blockNum;
			}
		}
	}

	public double SAE(Matrix actualBlock, Matrix inputBlock) {
		// pracuje se dvema bloky stejne velikosti
		double sae = 0;

		for (int i = 0; i < actualBlock.getRowDimension() - 1; i++) {
			for (int j = 0; j < actualBlock.getColumnDimension() - 1; j++) {
				sae += Math.abs(actualBlock.get(i, j) - inputBlock.get(i, j));
			}
		}
		sae /= ((actualBlock.getColumnDimension()) * (actualBlock.getRowDimension())); // tento krok chybi ve skriptech!

		return sae;
	}

	// getters and setters
	public Matrix getMotionVectorsFS() {
		return motionVectorsFS;
	}

	public Matrix getMotionVectorsOATS() {
		return motionVectorsOATS;
	}

	public Matrix getWideP_applyMotionVectors() {
		return wideP_applyMotionVectors;
	}

	public void setWideP_applyMotionVectors(Matrix wideP_applyMotionVectors) {
		this.wideP_applyMotionVectors = wideP_applyMotionVectors;
	}

	public void setWideP(Matrix wideP) {
		this.wideP = wideP;
	}
}
