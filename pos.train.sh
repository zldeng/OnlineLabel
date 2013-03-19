#train model with a single thread, we set compression ratio to be 0.2
nohup java -Xms15G -Xmx30G -cp OnlineLabel.jar ir.hit.edu.ltp.parser.OnlinePos \
-train -trainFile ./data/pos/train.conll06.pos \
-dicFile ./data/pos/conll06.pos.dic \
-model  ./model/conll06.model \
-iterator 40  \
-devFile ./data/pos/dev.conll06.pos.gold \
-compress 0.2 &

#train model with Multi-thread, we set compression ratio to be 0.2 and set thread number to be 10
nohup java -Xms15G -Xmx30G -cp OnlineLabel.jar ir.hit.edu.ltp.parser.OnlinePos \
-train -trainFile ./data/pos/train.conll06.pos \
-dicFile ./data/pos/conll06.pos.dic \
-model  ./model/conll06.model \
-iterator 40  \
-devFile ./data/pos/dev.conll06.pos.gold \
-compress 0.2 
-thread 10 &