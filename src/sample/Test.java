package sample;

import ir.hit.edu.ltp.ml.PosAP;
import ir.hit.edu.ltp.ml.SegAP;

import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

public class Test
{
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("./config/log4j.properties");

		// TODO Auto-generated method stub
		/**************** SEG **************/
		String segModel = "./models/pku-seg/pku.seg.model";
		String segDic = "./models/pku-seg/pku_training.dic.utf8";

		SegAP segger = new SegAP();
		segger.loadResource(segModel, segDic);

		//seg for a file using only one thread
		String segFile = "./data/seg/pku.test";
		String segResult = "./data/seg/pku.test.result";
		segger.segForFile(segFile, segResult);
		
		//seg for a file using Multi-Thread
		String segResult2 = "./data/seg/pku.test.thread.result";
		//thread nunber
		int threadNum = 5;
		segger.segForFile(segFile, segResult2,threadNum);

		//seg for a raw sentence
		String sen = "最后，我从北京祝大家新年快乐！";
		Vector<String> result = new Vector<String>();
		segger.segViterbiDecode(sen, result);
		System.out.println(result.toString());

		/**************** POS **************/
		String posModel = "./models/conll06-pos/conll06.pos.model";
		String posDic = "./models/conll06-pos/conll06.pos.dic";

		PosAP posTagger = new PosAP();
		posTagger.loadResource(posModel, posDic);

		//POS for a segmented file using only one thread
		String posFile = "./data/pos/test.conll06.seg";
		String posResult = "./data/pos/test.conll06.result";
		posTagger.PosForFile(posFile, posResult);
		
		//POS for a segmented file using only Multi-Thread
		String posResult2 = "./data/pos/test.conll06.thread.result";
		posTagger.PosForFile(posFile, posResult2,threadNum); 

		//POS for a segmented sentence
		Vector<String> posSen = new Vector<String>();
		posSen.add("我");
		posSen.add("爱");
		posSen.add("旅游");
		posSen.add("。");

		result = new Vector<String>();
		posTagger.posViterbiDecode(posSen, result);

		String str = "";
		for (int i = 0; i < posSen.size(); i++)
			str += posSen.elementAt(i) + "_" + result.elementAt(i) + " ";
		System.out.println(str);
	}

}
