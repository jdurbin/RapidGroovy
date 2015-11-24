/**
* Simplified bed graph
*/ 
class BedLine{
	def err = System.err
	
	def chr
	def start
	def end
	def state 
	def intstate
		
	def BedLine(line){
		def fields = line.split("\t")
		chr = fields[0]
		start = fields[1] as int
		end = fields[2] as int
		state = fields[3]
		def bits = state.split("_")
		intstate = bits[0] as int
	}	 
	
	def contains(x){
		if ((x >= start) && (x <= end)) return true;
		else return false
	}
}