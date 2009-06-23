function Matlab(obj)
{
    // implementation detail, to allow unwrapping.
    this.matlab = obj;
    this.command = "";
}
Matlab.prototype = {
    mEvalString : function(expression)
    {
        this.matlab.mEvalString(expression);
    },
    mEvalMFile : function(file)
    {
        this.matlab.mEvalMFile(file);
    },
    mMatrix2LaTeX : function()
    {
        return this.matlab.mMatrix2LaTeX();
    },
	 mGetArray: function(list)
    {
		if (arguments.length == 1)
			return this.matlab.mGetArrayLaTeX(arguments[0]); 
		else
			if (arguments.length == 3)
        		return this.matlab.mGetArray(arguments[0],arguments[1],arguments[2]);
    }
};
function Clpbrd(obj)
{
    // implementation detail, to allow unwrapping.
    this.clpbrd = obj;
}
Clpbrd.prototype = {
    showClipBoard : function(list)
    {
	if (arguments.length == 0)
        	this.clpbrd.showClipBoard();
	else
		if (arguments.length == 2)
        		this.clpbrd.showClipBoard2(arguments[0],arguments[1]);
		else
			if (arguments.length == 3)
   				this.clpbrd.showClipBoard3(arguments[0],arguments[1],arguments[2]);
    }
};

