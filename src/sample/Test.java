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
		String segModel = "./base/seg-10001-17000-retrain-1.model-19";
		String segDic = "./data/seg/pku.seg.dic";

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
		segger.segForFile(segFile, segResult2, threadNum);

		//seg for a raw sentence
		String sen = "只有在你最失魂落魄最失败无助的时候才会知道谁是朋友 ";
		Vector<String> result = new Vector<String>();
		segger.seg(sen, result);
		System.out.println(result.toString());

		/**************** POS **************/
		String posModel = "./conll06/conll06-decreace-new-wr.model-it-9";
		String posDic = "./conll06/conll06.pos.dic";

		PosAP posTagger = new PosAP();
		posTagger.loadResource(posModel, posDic);

		//POS for a segmented file using only one thread
		String posFile = "./conll06/test.conll06.seg";
		String posResult = "./conll06/test.conll06.seg.result0";
		posTagger.PosForFile(posFile, posResult);
		//POS for a segmented file using only Multi-Thread
		String posResult2 = "./data/pos/test.conll06.thread.result";
		posTagger.PosForFile(posFile, posResult2, threadNum);

		//POS for a segmented sentence
		String senStr = "# 是 一个 符号 。";
		String[] token = senStr.split(" ");
		Vector<String> segSen = new Vector<String>();
		for (String str : token)
			segSen.add(str);
		
		Vector<String> posResult3 = new Vector<String>();
		posTagger.pos(segSen, posResult3);

		String str = "";
		for (int i = 0; i < result.size(); i++)
			str += segSen.elementAt(i) + "_" + result.elementAt(i) + " ";
		System.out.println(str);
	}
}
