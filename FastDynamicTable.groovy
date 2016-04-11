import com.google.common.collect.*
import groovy.transform.ToString

/**
* A dynamic, potentially sparse, table of objects. <br><br>
* 
* FastDynamicTable implements all the methods from google HashBasedTable via 
* Groovy Meta Object Programming and delegation. 
* 
* FastDynamicTable adds functionality to read and write tables, access
* sugar, etc. 
* 
*/ 
@ToString
class FastDynamicTable {
	static def err = System.err
	
	// For the HashBasedTable. Using delegation instead of inheritance.
	HashBasedTable delegate; 
	def defaultVal = "null"
	
	def FastDynamicTable(){
		delegate = HashBasedTable.create()
	}
	
	def FastDynamicTable(fileName,delimiter){
		delegate = HashBasedTable.create()
		read(fileName,delimiter)
	}
	
	def numRows(){return(delegate.rowKeySet().size())}
	def numCols(){return(delegate.columnKeySet().size())}
	
	// Groovy MOP magic! 
	// intercepts method access and passes them to delegate
	// 
	def invokeMethod(String name,args){
		delegate.invokeMethod(name,args)
	}	
	
	// Treats table as a vector of counts 
	def increment(rowKey,colKey){
		def val = this[rowKey][colKey]
		if (val == null) {
			this[rowKey][colKey] = 1
			return(1)
		}else {
			this[rowKey][colKey] = val+1
			return(val+1)
		}
 	}
	
	// Allow [] notation
	def putAt(String rowKey,value){
		this[rowKey] = value
	}
	
	// Allow [] notation. 
	def getAt(String rowKey){
		return(delegate.row(rowKey))
	}		
	
	
	def eachRow(Closure c){
		delegate.rowMap().each{rowKey,columnMap->
			c(columnMap.values())
		}
	}
	
	def eachRowKeyWithValues(Closure c){
		delegate.rowMap().each{rowKey,columnMap->
			c(rowKey,columnMap.values())
		}
	}
	
	def eachRowKeyWithColumnMap(Closure c){
			delegate.rowMap().each{rowKey,columnMap->
				c(rowKey,columnMap)
			}
	}
	
	
	/***
	* Print the table to stdout
	*/ 
	def print(){
		def colKeys = delegate.columnKeySet()
		def rowKeys = delegate.rowKeySet()
	 	print "Features\t"
		println colKeys.join("\t")				
		rowKeys.each{r->		
			print "${r}\t"
			def rowvals = colKeys.collect{c->delegate.get(r,c) ?: defaultVal}
			println rowvals.join("\t")
		}
	}												
	
	/***
	* Print sorted
	*/ 
	def printSorted(){
		def colKeys = delegate.columnKeySet()
		def rowKeys = delegate.rowKeySet()
		
		colKeys = colKeys.sort()
		rowKeys = rowKeys.sort()		
	 	print "Features\t"
		println colKeys.join("\t")				
		rowKeys.each{r->		
			print "${r}\t"
			def rowvals = colKeys.collect{c->delegate.get(r,c) ?: defaultVal}
			println rowvals.join("\t")
		}	
	}
	
	
	// 40 seconds... 259 x 295
	// 6 second without rowvals= and w.writeLine lines!
	def write(fileName,delimiter){
		
		new File(fileName).withWriter{w->
		
			def colKeys = delegate.columnKeySet()
			def rowKeys = delegate.rowKeySet()
			w.write "Features$delimiter"
			w.writeLine colKeys.join(delimiter)		

			rowKeys.each{r->		
				w.write "${r}$delimiter"
				def rowvals = colKeys.collect{c->delegate.get(r,c) ?: defaultVal}
				w.writeLine rowvals.join(delimiter)
			}
		}
	}
	
	// grab rowmap and write out rowmap.values  9803x295 in 6.9 sec. 
	// 
	def write2(fileName,delimiter){
		
		new File(fileName).withWriter{w->
		
			def colKeys = delegate.columnKeySet()
			def numCols = colKeys.size()
			def rowKeys = delegate.rowKeySet()
			w.write "Features$delimiter"
			w.writeLine colKeys.join(delimiter)		

			rowKeys.each{r->		
				w.write "${r}$delimiter"
				def rowmap = delegate.row(r)
				
				// 206 x 295  33.5s
				//def rowvals = colKeys.collect{c->
				//	rowmap[c] ?: defaultVal					
				//}				
				
				// If sizes match just write it out, otherwise find the missing values
				// and replace them with default values. 
				// In case where there are no missing values, this takes 7s.  9803x295
				// Time will go up dramatically with missing values. 
				if (rowmap.size() == numCols){
					w.writeLine rowmap.values().join(delimiter)
				}else{
					def rowvals = colKeys.collect{c->
						rowmap[c] ?: defaultVal					
					}
					w.writeLine rowvals.join(delimiter)					
				}							
			}
		}
	}


	/***
	* Reads in a table of values as a GTable
	*/ 
	def read(fileName,delimiter){
		new File(fileName).withReader{r->
			def headings = r.readLine().split(delimiter)
			headings = headings[1..-1] // omit Feature label...

			r.splitEachLine(delimiter){fields->
				def rowName = fields[0]
				headings.eachWithIndex{h,i->
					delegate.put(rowName,h,fields[i+1])
				}
			}		
		}
		return(this)
	}

}