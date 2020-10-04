JFLAGS = -d bin -cp :audio.jar:bin
#                      > link with audio.jar
#                               > link with bin content
JC = javac
JR = java
SRC = $(shell find ./src -name "*.java")
#                   > dans le dossier src
#                          > dont le nom est
SRC := $(patsubst ./%, %, $(SRC))
#         > replace (pattern substitution)
#              > "./foo"
#                  (by)
#                     > "foo"
#                           > in $(SRC)

# Suffix Replacement within a macro: 
# $(name:string1=string2)
CLASSES = $(SRC:.java=.class)
CLASSES := $(patsubst src/%, bin/%, $(CLASSES))

all: $(CLASSES)

bin/%.class: src/%.java
	$(JC) $(JFLAGS) $<	

clean:
	rm *.class

