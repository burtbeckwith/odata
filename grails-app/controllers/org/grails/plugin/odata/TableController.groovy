package org.grails.plugin.odata
import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException

/**
 * TableController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class TableController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	def show() {
		def table = params.id? Table.get(params.id): Table.first()
		[headers:table.getColumnsSorted(),table:params.id]
	}

	def getItems() {
		def table = Table.get(params.table)
		def columns = table.getColumnsSorted()

		def dataToRender = [:]
		dataToRender.sEcho = params.sEcho

		def sortProperty = columns[params.iSortCol_0 as Integer].name
		def sortDir = params.sSortDir_0?.equalsIgnoreCase('asc') ? 'asc' : 'desc'

		def data = table.getData(params.iDisplayLength, params.iDisplayStart, "${sortProperty} ${sortDir}", getFilterQuery(columns))
		
		dataToRender.iTotalRecords = data.__count
		dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords

		dataToRender.aaData=data.results
		
		render dataToRender as JSON
	}
	
	def getFilterQuery(columns) {
		StringBuilder filter = new StringBuilder()
		columns.eachWithIndex { descr, i ->
			def value = params.get("sSearch_"+i);
			if (value) {
				if (filter.length() > 0)
					filter << " and "
				filter << "substringof('" << value << "', " << descr.name << ")"
			}
		}
		return filter.toString()
	}
    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [tableInstanceList: Table.list(params), tableInstanceTotal: Table.count()]
    }

    def create() {
        [tableInstance: new Table(params)]
    }

    def save() {
        def tableInstance = new Table(params)
        if (!tableInstance.save(flush: true)) {
            render(view: "create", model: [tableInstance: tableInstance])
            return
        }

		flash.message = message(code: 'default.created.message', args: [message(code: 'table.label', default: 'Table'), tableInstance.id])
        redirect(action: "show", id: tableInstance.id)
    }

    def show_old() {
        def tableInstance = Table.get(params.id)
        if (!tableInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'table.label', default: 'Table'), params.id])
            redirect(action: "list")
            return
        }

        [tableInstance: tableInstance]
    }

    def edit() {
        def tableInstance = Table.get(params.id)
        if (!tableInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'table.label', default: 'Table'), params.id])
            redirect(action: "list")
            return
        }

        [tableInstance: tableInstance]
    }

    def update() {
        def tableInstance = Table.get(params.id)
        if (!tableInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'table.label', default: 'Table'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (tableInstance.version > version) {
                tableInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'table.label', default: 'Table')] as Object[],
                          "Another user has updated this Table while you were editing")
                render(view: "edit", model: [tableInstance: tableInstance])
                return
            }
        }

        tableInstance.properties = params

        if (!tableInstance.save(flush: true)) {
            render(view: "edit", model: [tableInstance: tableInstance])
            return
        }

		flash.message = message(code: 'default.updated.message', args: [message(code: 'table.label', default: 'Table'), tableInstance.id])
        redirect(action: "show", id: tableInstance.id)
    }

    def delete() {
        def tableInstance = Table.get(params.id)
        if (!tableInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'table.label', default: 'Table'), params.id])
            redirect(action: "list")
            return
        }

        try {
            tableInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'table.label', default: 'Table'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'table.label', default: 'Table'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
}
