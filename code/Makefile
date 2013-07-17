ifndef $(NAME)
  NAME=DEFAULT
endif

prefix=../state/execs

% : %.java Makefile
	rm -f *.class
	javac -cp .:jars/guava-14.0.1.jar $<
	mkdir -p classes/$(NAME)
	mv *.class classes/$(NAME)/
	mkdir -p $(prefix)/$(NAME)

clean:
	rm -f classes/$(NAME)/*.class

run:
	java -cp .:jars/guava-14.0.1.jar:classes/$(NAME) Alignment
