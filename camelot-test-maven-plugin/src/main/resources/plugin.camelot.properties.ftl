# suppress inspection "UnusedProperty" for whole file
camelot.pluginLoader=camelot-loader-plugin
testSrcResDir=${testSrcResDir}
srcResDir=${srcResDir}

camelot.input.uri=${mainInputUri}
camelot.output.uri=${mainOutputUri}

#plugins.config.path=file:${srcResDir}/camelot-config.xml
plugins.config.path=classpath*:/camelot.xml
plugins.config.updatePolicy=never
plugins.local.repository=${localRepo}
plugins.remote.repositories=${remoteRepos}

