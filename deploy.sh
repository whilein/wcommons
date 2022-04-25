#!/bin/sh
mvn --no-transfer-progress --batch-mode -T 1C -Pdeploy -DskipTests=true -Dmaven.test.skip clean deploy