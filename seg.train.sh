nohup java -Xms15G -Xmx30G -cp OnlineLabel-1.3.jar ir.hit.edu.ltp.parser.OnlineSeg \
-train  -trainFile ./data/seg/pku.train  \
-model ./model/pku.seg.model \
-devFile ./data/seg/pku.test.gold  \
-dicFile ./data/seg/pku.seg.dic \
-iterator 40 \
-compress 0.2 &
