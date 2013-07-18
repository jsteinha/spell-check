ifndef $(NAME)
  NAME=DEFAULT
endif

prefix=../state/execs
include=jars/guava-14.0.1.jar:jars/fig.jar

% : %.java Makefile
	rm -f *.class
	javac -cp .:$(include) $<
	mkdir -p classes/$(NAME)
	mv *.class classes/$(NAME)/
	mkdir -p $(prefix)/$(NAME)

clean:
	rm -f classes/$(NAME)/*.class

run:
	java -cp .:$(include):classes/$(NAME) Main -execPoolDir $(prefix)/$(NAME) -Main.experimentName $(NAME) -Main.beamSize $(BEAM_SIZE)
