modules = {
   
	'datatables' {
		
		resource url: [dir: 'js' , file: 'jquery.dataTables-1.9.4.js' ,plugin:'odata']
		resource url: [ dir: 'js' , file:'DT_bootstrap.js' ,plugin:'odata']
		resource url: [ dir: 'css' , file:'DT_bootstrap.css' ,plugin:'odata']
	}
	println " || Adding OData Resources ...........||"
}