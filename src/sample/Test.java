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
		String segModel = "./model/pku.seg.model-it-29-18";
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
		String posModel = "./model/conll06.pos.model-18";
		String posDic = "./data/pos/conll06.pos.dic";

		PosAP posTagger = new PosAP();
		posTagger.loadResource(posModel, posDic);

		//POS for a segmented file using only one thread
		String posFile = "./data/pos/test.conll06.seg";
		String posResult = "./data/pos/test.conll06.result";
		posTagger.PosForFile(posFile, posResult);

		//POS for a segmented file using only Multi-Thread
		String posResult2 = "./data/pos/test.conll06.thread.result";
		posTagger.PosForFile(posFile, posResult2, threadNum);

		//POS for a segmented sentence
		String posSen = "我 爱 哈尔滨 ！";
		String[] segSen = posSen.split(" ");

		result = new Vector<String>();
		posTagger.pos(segSen, result);

		String str = "";
		for (int i = 0; i < result.size(); i++)
			str += segSen[i] + "_" + result.elementAt(i) + " ";
		System.out.println(str);
	}
}
