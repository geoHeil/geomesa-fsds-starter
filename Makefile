make run:
	./gradlew shadowJar && \
	spark-submit --verbose \
	--class Job1 \
	--master 'local[1]' \
	build/libs/geomesa-fsds-starter-all.jar

make read:
	./gradlew shadowJar && \
	spark-submit --verbose \
	--class JobRead \
	--master 'local[1]' \
	build/libs/geomesa-fsds-starter-all.jar