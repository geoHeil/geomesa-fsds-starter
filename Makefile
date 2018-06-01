make run:
	./gradlew shadowJar && \
	spark-submit --verbose \
	--class Job1 \
	--master 'local[2]' \
	build/libs/geomesa-fsds-starter-all.jar