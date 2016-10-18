package test

import org.grails.plugin.queuekit.QueuekitUserService
import org.grails.plugin.queuekit.ReportsQueue
import org.grails.plugin.queuekit.priority.Priority

class MyUserService extends QueuekitUserService {

	/*
	 * Override this service and method to return your real user
	 * must return their userId 
	 */
	@Override
	Long getCurrentuser() {
		return 1L
	}
	
	/*
	 * Overrider this method to then ensure superUser
	 * Privileges are only given to superUser's as per your definition
	 * if it is a security group or some user role.
	 */
	boolean isSuperUser(Long userId) {
		//println "== super user switched off in my test override"
		//this class extends QueuekitUserService
		// Also in the current project conf/spring/resources.groovy this service then overrides actual service:
		// queuekitUserService(test.MyUserService)
		return true //false //userId==1L
	}
	
	
	/*
	 * Override this to get the real users UserName
	 *
	 */
	String getUsername(Long userId) {
		//Return actual user's username
		return 'vahid'
	}
	
	/*
	 * Override this to return a locale for your actual user
	 * when running reports if you have save their locale on the DB
	 * you can override here it will be defaulted to null and set to
	 * predfined plugin value in this case
	 *
	 */
	Locale  getUserLocale(Long userId) {
		return Locale.UK
	}
	
	
	
	/*
	 * Another method to override
	 * Whilst you can configure a report to have LOW priority
	 * It could be that it needs to be LOW for long term date range
	 * but HIGH for a short 1 day lookup
	 *
	 * This is a final stage before actual priority is selected
	 * which if not found here will be actual report default
	 * as defined in configuration if not by plugin default choice LOW
	 */
	Priority reportPriority(ReportsQueue queue, Priority givenPriority, params) {
		Priority priority
		
		if (queue.hasPriority()) {
						
			priority = queue.priority ?: queue.defaultPriority
			//println "-- priority = ${priority} qp : ${queue.priority} qd: ${queue.defaultPriority} vs ${givenPriority}"
			
			if (givenPriority < priority) {
				priority = givenPriority
			}
			
			//if (priority > Priority.HIGHEST) {
				switch (queue.reportName) {
					case 'tsvExample2':
						priority = checkReportPriority(priority,params)
						break
					case 'csvExample':
						// Actual check in Report3Bean launched by index8
						// which launches csvExample call and has input for
						// from/to Dates
						//println "--LAST priority = ${priority}"
						priority = checkReportPriority(priority,params)
						//println "--LAST priority after = ${priority}"
						break
					case 'xlsExample1':
						priority = checkReportPriority(priority,params)
						break
				}
			//}
		}
		return priority
	}
	
	/*
	 * A demo of how to try to override a report's priority
	 * in this case based on from/to Dates
	 *
	 * It maybe you have more refined range periods and a rule that
	 * anything beyond a certain level regardless of current position
	 *
	 * This is really a scribble but maybe a good starting point
	 *
	 */
	Priority checkReportPriority(Priority priority,params) {
		//println "params ${params} ${params.fromDate} vs ${params.toDate} vs ${params.toDate.getClass()}"
		if (params.fromDate && params.toDate) {
			Date toDate = parseDate(params.toDate)
			Date fromDate = parseDate(params.fromDate)
			int difference = toDate && fromDate ? (toDate - fromDate) : null
			if (difference||difference==0) {
				if (difference <= 1) {
					//println "-block 1"
					// 1 day everything becomes HIGH priority
					priority = Priority.HIGH
				} else if  (difference >= 1 && difference <= 8) {
					//println "-block 2"
					
					if (priority == Priority.HIGHEST) {
						priority = Priority.HIGH
					} else if (priority >= Priority.MEDIUM) {
						priority = priority.value--
					}
				} else if  (difference >= 8 && difference <= 31) {
					if (priority <= Priority.HIGH) {
						priority = Priority.MEDIUM
					} else if (priority >= Priority.LOW) {
						priority = priority.MEDIUM
					}
				} else if  (difference >= 31 && difference <= 186) {
					if (priority >= Priority.MEDIUM && priority <= Priority.HIGHEST) {
						priority = priority.value--
					} else if (priority >= Priority.LOW) {
						priority = Priority.MEDIUM
					}
				} else if  (difference >= 186) {
					if (priority <= Priority.HIGH) {
						priority = priority.value--
						
					} else if (priority >= Priority.LOW) {
						priority = Priority.MEDIUM
					}
				}
			}
		}
		return priority
	}
	
}
