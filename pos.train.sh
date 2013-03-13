nohup java -Xms15G -Xmx30G -cp OnlineLabel.jar ir.hit.edu.ltp.parser.OnlinePos \
-train -trainFile ./data/pos/train.conll06.pos \
-dicFile ./data/pos/conll06.pos.dic \
-model  ./model/conll06.model \
-iterator 40  \
-devFile ./data/pos/dev.conll06.pos.gold \
-compress 0.2 &