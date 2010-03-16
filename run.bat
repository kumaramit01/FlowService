@echo off
java -Djava.util.logging.config.file=logging.properties -classpath .;${project.build.finalName}.jar org.imirsel.nema.flowservice.FlowServiceApp