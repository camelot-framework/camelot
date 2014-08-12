# suppress inspection "UnusedProperty" for whole file
camelot.pluginLoader=LOADER-PLUGIN
testSrcResDir=${testSrcResDir}
srcResDir=${srcResDir}

camelot.input.uri=${mainInputUri}
camelot.output.uri=${mainOutputUri}

#plugins.config.path=file:${srcResDir}/camelot-config.xml
plugins.config.path=classpath*:/camelot.xml
plugins.config.updatePolicy=never
plugins.local.repository=${localRepo}
plugins.remote.repositories=${remoteRepos}

jms.broker.list=(${activemqBrokers})?randomize=false&priorityBackup=true&jms.sendAcksAsync=true&jms.useAsyncSend=true&jms.dispatchAsync=true


######################################
# ---------------------------------- #
# Hazelcast

# Group name and password
hazelcast.group.name=dev
hazelcast.group.password=dev-pass

# Hazelcast port, autoincrement property should be true to run several nodes on the same host
hazelcast.port=6801
hazelcast.port.auto.increment=true
