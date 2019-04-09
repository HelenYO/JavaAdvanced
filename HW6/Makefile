BUILD_DIR=build
MODULES_DIR=modules
MODULE_NAME=ru.ifmo.rain.ilina.implementor
MODULE_PATH=$(MODULES_DIR)/$(MODULE_NAME)
SOURCES=$(MODULE_PATH)/module-info.java $(MODULE_PATH)/ru/ifmo/rain/ilina/implementor/*.java
ARTIFACTS_DIR=artifacts
LIB_DIR=lib
MANIFEST=resources/implementor.mf
DOC_DIR=doc
JDK_DOC_URL=file:///usr/share/doc/java-openjdk/api

.PHONY: build jar clean javadoc

build: $(SOURCES)
	javac -d $(BUILD_DIR) -p $(ARTIFACTS_DIR):$(LIB_DIR) --module-source-path $(MODULES_DIR) -m $(MODULE_NAME)
	
jar: build $(MANIFEST)
	cp $(MANIFEST) $(BUILD_DIR)/$(MODULE_NAME)
	cp -r $(BUILD_DIR)/info.kgeorgiy.java.advanced.implementor/info $(BUILD_DIR)/$(MODULE_NAME)
	cd $(BUILD_DIR)/$(MODULE_NAME) && jar -cmf implementor.mf $(MODULE_NAME).jar module-info.class ru info
	mv $(BUILD_DIR)/$(MODULE_NAME)/$(MODULE_NAME).jar .

clean: 
	rm -rf $(BUILD_DIR)
	rm -f $(MODULE_NAME).jar
	rm -rf $(DOC_DIR)
	
javadoc: $(SOURCES)
	javadoc -link $(JDK_DOC_URL) -private -d $(DOC_DIR) -p $(LIB_DIR):$(ARTIFACTS_DIR) --module-source-path $(MODULES_DIR) --module $(MODULE_NAME) info.kgeorgiy.java.advanced.implementor