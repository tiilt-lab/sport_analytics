Summer19/
	LSTM            : example LSTM
	Activity        : the main model
	grouping_script : script for
	HARDataset/     : data for example LSTM
	DIMDataset/
		raw_{activity}     : the data as recorded from empatica
		clean_{activity}   : just x, y, z, and the activity label
		clean_combined     : all cleaned activities combined
		stream_{dimension} : all data for an individual dimension
		windows_{dimesion} : variable size windows of individual dimensions of data, potentially with overlap
		windows_actv       : the activity label for each window

Activities
	0: null
	1: shooting
	2: dribbling