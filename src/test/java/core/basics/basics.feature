Feature: Basic features
		This tests basic NileDB Core features.

Background:
* def config = read('classpath:config.json')
* url config.url
* path 'graphql'

Scenario: get system average load
Given text query = 
"""
{
	getSystemAverageLoad
}
"""
And request { query: '#(query)' }
And header Accept = 'application/json'
When method post
Then status 200
And match response == { data: #present }
And match response == { error: #notpresent }
And match response.data == { getSystemAverageLoad: #number }
