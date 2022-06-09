#rm -r bin/*
cd src
echo "compiling..."
javac -d ../bin --module-path $1 --add-modules javafx.controls com/orangomango/tictactoe/MainApplication.java
cp -r ../res/* ../bin
cd ../bin
echo "executing..."
java --module-path $1 --add-modules javafx.controls com.orangomango.tictactoe.MainApplication
