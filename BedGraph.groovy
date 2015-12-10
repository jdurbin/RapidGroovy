import BedLine.*
import java.util.zip.GZIPInputStream

/**
* Class for manipulating a simple 4 field BedGraph...
*
* chr10   56599200        56600000        9_Het
* chr10   56600000        56635200    import BedLine.*
import java.util.zip.GZIPInputStream

/**
* Class for manipulating a simple 4 field BedGraph...
*
* chr10   56599200        56600000        9_Het
* chr10   56600000        56635200        15_Quies
* chr10   56635200        56636800        9_Het
* chr10   56636800        56672000        15_Quies
* chr10   56672000        56675000        9_Het
* chr10   56675000        56687000        15_Quies
* chr10   56687000        56687800        9_Het
* chr10   56687800        56721000        15_Quies
* chr10   56721000        56723000        9_Het
* chr10   56723000        56748800        15_Quies
* chr10   56748800        56749800        9_Het
* chr10   56749800        56760400        15_Quies
*/

class BedGraph extends ArrayList{
	
	def err = System.err

	def BedGraph(bedFileName){read(bedFileName)}
	
	def chrMax(chr){
		int maxChr = 0;
		this.each{bed->
			if ((bed.chr == chr) && (bed.end > maxChr)) maxChr = bed.end  
		}
		return(maxChr)
	}
	
	def toLinearArray(chr){
		def maxChr = chrMax(chr)
		def chrArray = new int[maxChr+10]
		
		for(int i = 0;i < this.size();i++){
			def bed = (this)[i]
			if (bed.chr == chr){
				for(int j = bed.start;j <= bed.end;j++){
					chrArray[j] = bed.intstate
				}
			}
		}
		return(chrArray)
	}
	

	def read(bedFileName){
		// Read Bed file...
		err.print "Reading $bedFileName..."
		def fis = new FileInputStream(bedFileName)
		GZIPInputStream gzip = new GZIPInputStream(fis)
		BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
		br.eachLine{line->
			this << new BedLine(line)
		}
		err.println " ${this.size()} items read."
	}
	
	def find(chr,int target){
		// Find our region of bed file...
		int startBedIdx = -1;
		int numBed = this.size()
		for(int bIdx = 0;bIdx < numBed; bIdx++){
			def bed = this[bIdx]
			if (bed.chr == chr){
				if (bed.contains(target)){
					startBedIdx = bIdx
					break;
				}
			}
		}
		return(startBedIdx);
	}
	
	def keySet(){
		def keyset = [] as Set
		this.each{bed->
			keyset << bed.state
		}
		return(keyset)
	}
	
}
