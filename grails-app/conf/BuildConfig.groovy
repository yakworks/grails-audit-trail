grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
	}

	plugins {
		build ':release:2.2.1', ':rest-client-builder:1.0.3', {
			export = false
		}

		compile (":hibernate:$grailsVersion"){
			export = false
		}
		
		compile (':spring-security-core:1.2.7.3'){
			export = false
		}
	}
}
