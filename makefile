JAR_PKG = 15640_DS_Lab1.jar

ENTRY_POINT = migratableProcess/ProcessManager

RES_DIR = yes

SOURCE_FILES = \
migratableProcess/Configuration.java \
transactionIO/TransactionalFileInputStream.java \
transactionIO/TransactionalFileOutputStream.java \
migratableProcess/MigratableProcess.java \
migratableProcess/ProcessOperation.java \
migratableProcess/ProcessStatus.java \
migratableProcess/ProcessInfo.java \
migratableProcess/Serialization.java \
slave/SlaveInfo.java \
slave/SlaveMessage.java \
master/MasterMessage.java \
master/MasterServer.java \
slave/SlaveServer.java \
migratableProcess/ProcessManager.java \
StopWords.java \
FindHotWords.java \
Sum.java \

JAVAC = javac

JFLAGS = -encoding UTF-8

vpath %.class bin
vpath %.java src

Default:
	@echo "make new: new project, create src, bin, res dirs."
	@echo "make build: build project."
	@echo "make clean: clear classes generated."
	@echo "make rebuild: rebuild project."
	@echo "make run: run your app."
	@echo "make jar: package your project into a executable jar."

build: $(SOURCE_FILES:.java=.class)

%.class: %.java
	$(JAVAC) -cp bin -d bin $(JFLAGS) $<

rebuild: clean build

.PHONY: new clean run jar

new:
ifeq ($(RES_DIR),yes)
	mkdir -pv src bin res
else
	mkdir -pv src bin
endif

clean:
	rm -frv bin/*

run:
	java -cp bin $(ENTRY_POINT)

jar:
ifeq ($(RES_DIR),yes)
	jar cvfe $(JAR_PKG) $(ENTRY_POINT)  -C bin . res
else
	jar cvfe $(JAR_PKG) $(ENTRY_POINT) -C bin .
endif
