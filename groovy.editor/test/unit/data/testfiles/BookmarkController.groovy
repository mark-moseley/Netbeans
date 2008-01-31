import org.grails.bookmarks.*

class BookmarkController extends SecureController {
	
    def scaffold = Bookmark 
	
    DeliciousService deliciousService
	
    Map params = [:]
    //Map flash = [:]
    //Map session = [:]
	
    def create = {
        def b = new Bookmark()
        b.properties = params
        def suggestions = b.url ? getSuggestions(b.url, b) : []
        return [bookmark:b, suggestions:suggestions]	
    }
    
    def preview = {
        def b = Bookmark.get(params.id)
        render {
            iframe(src:b.url,width:640,height:480,border:0, 'iframes not supported by browse')
        }
    }
    
    def edit = {
        def b = Bookmark.get(params.id) 
        if(b) {
            return [bookmark:b, suggestions: getSuggestions(b.url,b) ]
        }
        else {
            render "bookmark not found for id ${params.id}"
        }
    }           
	
    def delete = {
        def b = Bookmark.get(params.id)
        if(b) {
            b.delete()
            flash.message = "Bookmark ${b.id} deleted."
            redirect(action:list)
        }
        else {
            render "Bookmark not found"
        }
    }
		
    def update = {
        def b = Bookmark.get(params.id)
        if(b) {
            b.properties = params 
            addTags(b)
            if(b.save()) {
                render(template:'bookmark', model:[bookmark:b])	
            }
            else {
                render(view:'edit', model:[bookmark:b])
            }			
        }
        else {
            render "Bookmark not found"
        }
    }
    
    def save = {
        def b = new Bookmark()
        b.properties = params
        b.user = User.get(session.user.id)

        if(b.save()) {
            if(params.tagTokens) {
                addTags(b)
                b.save()
            }			
            redirect(action:list,id:b.id)
        }
        else {
            render(view:'create',model:[bookmark:b])
        }
    }
	
    private addTags(Bookmark b) {   
        def tags = params.tagTokens?.trim()
        if(tags?.indexOf(' ')) {
            tags = tags.split(' ').toList()
            tags = tags.unique()
            tags.each {
                Tag t = Tag.findByName(it)
                if(!t)t = new Tag(name:it).save()

                b.addToTags(new TagReference(bookmark:b, tag:t, user:b.user))
            }
        }
        else {
            Tag t = Tag.findByName(tags)
            if(!t)t = new Tag(name:tags).save()

            b.addToTags( new TagReference(bookmark:b, tag:t, user:b.user) )
        }		
    }    
	
    def list = {
        if(!params.max)params.max=10        
        def fromDelicious                         
        try {
            fromDelicious = deliciousService?.findRecent(session.user)
        }
        catch(Exception e) {
            println e.message
            e.printStackTrace(System.out)
            //log.error("Error communication with del.icio.us service: ${e.message}",e)
        }
		    
        [bookmarkList: Bookmark.findAllByUser(User.get(session['user'].id), params ), deliciousList: fromDelicious ]
    }
	
    def search = {
        def criteria = Bookmark.createCriteria()
        def t = Tag.findByName(params.q)
        def bookmarks = criteria.list {
            or {
                ilike('title',"%${params.q}%".toString())
                ilike('notes',"%${params.q}%".toString())
                if(t) {
                    tags {
                        eq('tag', t)
                    }					
                }				
            }
        }    
        def fromDelicious
        try {
            fromDelicious = deliciousService?.findAllForTag(params.q,session.user) 
        }
        catch(Exception e) {
            println e.message
            e.printStackTrace(System.out)			
            //log.error("Error communication with del.icio.us service: ${e.message}",e)
        }		
		
        render(view:'list',model:[ bookmarkList:bookmarks.unique(),
                deliciousResults:fromDelicious] )
    }
	
    def updateNotes = {		
        def b = Bookmark.get(params.id)
        if(b) {
            b.notes = params.notes
            b.save()
        }
        render( params.notes )
    }
	
    def suggestTag = {    
        if(params.value?.trim()) {
            if(!params.value?.startsWith("http://")) params.value = "http://${params.value}"

            def bookmark = params.id ? Bookmark.get(params.id) : null 
            try {
                def url = bookmark ? bookmark.url : new URI(params.value)
                def tags = getSuggestions(url,bookmark)

                render(template:'suggest',model:[tags:tags,bookmark:bookmark])							
            }   	                                                                    
            catch(Exception e) {
                log.debug(e.message , e)
                render ""
            }
        } 
        else {
            render ""
        }
    }
    
    private printParams(params, unusedParam) {
        params.each {
            println it
        }
    }
    
    private getSuggestions(url,bookmark) {
        def tagRefs = TagReference.findAll('from TagReference where bookmark.url = ?', [url])
        def tags
        if(bookmark) {
            tags = tagRefs.findAll { bookmark.tags?.contains(it.tag) }.tag.unique()			
        }
        else {
            tags = tagRefs.tag.unique()
        }
        return tags		
    }
}
