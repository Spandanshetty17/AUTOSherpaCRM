package entity

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator


/**
 * Created By 1524 on 10/1/2020
 */
class CallInfo : Parcelable {
    var callCount: String? = null
    var dealerCode: String? = null
    var noResponseScheduledCallDate: String? = null
    var noServiceReason: String? = null
    var followUpDate: String? = null
    var followUpTime: String? = null
    var pickUpAddress: String? = null
    var isFollowupRequired: String? = null
    var customerName: String? = null
    var currentAddress: String? = null
    var appointmentType: String? = null
    var permanentAddress: String? = null
    var officeAddress: String? = null
    var customerEmail: String? = null
    var customerPhone: String? = null
    var doa: String? = null
    var dob: String? = null
    var agentName: String? = null
    var callType: String? = null
    var freeService: String? = null
    var lastCallDate: String? = null
    var nextCallDate: String? = null
    var notes: String? = null
    var pickUpRequired: String? = null
    var serviceScheduled: String? = null
    var serviceScheduledTime: String? = null
    var serviceScheduledDate: String? = null
    var status: String? = null
    var appointmentDate: String? = null
    var defectDetails: String? = null
    var labourDetails: String? = null
    var lastServiceDate: String? = null
    var milege: String? = null
    var model: String? = null
    var nextServiceDue: String? = null
    var saleDate: String? = null
    var serviceAdvisor: String? = null
    var serviceType: String? = null
    var technician: String? = null
    var vehicleNumber: String? = null
    var lastBatteryChangeDate: String? = null
    var lastDrumBrakeChange: String? = null
    var lastOilChange: String? = null
    var vasNotes: String? = null
    var filePath: String? = null
    var callDate: String? = null
    var callTime: String? = null
    var callDuration: String? = null
    var latitude: String? = null
    var longitude: String? = null
    var comments: String? = null
    var isCallMade: String? = null
    var mediaFile: String? = null
    var callTypePicId = 0
    var vehicalRegNo: String? = null
    var totalVisitCount: String? = null
    var idealVisitCount: String? = null
    var visitSuccessRate: String? = null
    var lastVisitMileage: String? = null
    var lastVisitDate: String? = null
    var nextServiceDueDateForLastVisitDate: String? = null
    var nextServiceDueDateForMileage: String? = null
    var isFollowUpDone: String? = null
    var averageMileage: String? = null

    private fun setCallStatus(callStatus: String) {
        this.callStatus = callStatus
    }

    private var callStatus: String? = null
    private var noResponseOfCallMade: String? = null
        set

    /*public String getLastServicedate() {
        return lastServicedate;
    }

    public void setLastServicedate(String lastServicedate) {
        this.lastServicedate = lastServicedate;
    }*/
    /*private String lastServicedate;*/
    var lastServiceMileage: String? = null
    var lastServiceType: String? = null
    var lastVisitType: String? = null
    var averageRunning: String? = null
    var serviceNoShowPeriod: String? = null
    var serviceDueBasedonMileage: String? = null
    var serviceDueBasedonTenure: String? = null
    var nextServiceType: String? = null
    var forecastLogic: String? = null
    var variant: String? = null
    var dueDate: String? = null
    var district: String? = null
    var city: String? = null
    var makeCallFrom: String? = null
    var roNumber: String? = null
    var roDate: String? = null
    var salutation: String? = null
    var customerCategory: String? = null
    var attendproperly: String? = null
    var workdoneproperly: String? = null
    var overallinfrastrucure: String? = null
    var workdonexplanation: String? = null
    var washingquality: String? = null
    var overallrating: String? = null
    var customersatisfication: String? = null
    var firebaseKey: String? = null
    var firebaseRef: String? = null
    var coverNoteDate: String? = null
    var nextRenewalDate: String? = null
    private var insuranceCompany: String? = null
    var coverNote: String? = null
    private var lastIDV: String? = null
    var ncBPercentage: String? = null
    var ncBAmount: String? = null
    var odPercentage: String? = null
    var odAmount: String? = null
    var premiumAmount: String? = null
    var serviceLocation: String? = null
    var co_Dealer_Outside: String? = null
    var mileageAtService: String? = null
    var mileage: String? = null
    var reason: String? = null
    var dealerName: String? = null
    var droppedcount: String? = null
    var noncontactautodialDate: String? = null
    var noncontactautodialTime: String? = null
    var reasonForHTML: String? = null
    var dateOfService: String? = null
    var insuredParty: String? = null
    var insuranceDueDate: String? = null
    var alternateMobileno: String? = null
    var alternateVehicleRegno: String? = null
    var uniqueDataCallInit: String? = null
    var booked: String? = null
    var received: String? = null
    var noShow: String? = null
    var totalAssignedCalls: String? = null
    private var conversionRate: String? = null
    var pendingCalls: String? = null
    var uniqueidForCallSync = 0
    var serviceBooked: String? = null
    var pickupStarted: String? = null
    var pickupPointReached: String? = null
    var vehiclePicked: String? = null
    var deliveredAtws: String? = null
    var trackStatus: String? = null
    var cancelStatus: String? = null
    var lastDate: String? = null
    var customerCRN: String? = null
    var fuelType: String? = null
    var loyaltyType: String? = null
    var anniversary: String? = null
    var vehicleColor: String? = null
    var pickupOrDrop: String? = null
    var dropStarted: String? = null
    var deliveredToCustomer: String? = null
    var interactionDate: String? = null
    var interactionTime: String? = null
    var userId: String? = null
    var vehicleId: String? = null
    var serviceBookedId: String? = null
    var vehicleRegNo: String? = null
    var custId: String? = null
    var nextServiceDate: String? = null
    var jobCardNumber: String? = null
    var ageOfVehicle = 0
    var daysBetweenVisit = 0
    var chassisNo: String? = null
    var afterServiceSatisfication: String? = null
    var recentServiceSatisfication: String? = null
    var afterServiceComments: String? = null
    var recentServiceComments: String? = null
    var billDate: String? = null
    var workshop: String? = null
    var assignedId: String? = null
    var alreadyservicedatId: String? = null
    var ringingTime = 0.0
    var ringTime: String? = null
    var creName: String? = null

    //Insurance 07112017
    var policyNo: String? = null
    var lastPremium: String? = null
    var renewalType: String? = null
    var renewalMode: String? = null
    var addOns: String? = null
    var idvPercentage: String? = null
    var premium: String? = null
    var appointmentTime: String? = null
    var paymentType: String? = null
    var paymentReference: String? = null
    var addressReference: String? = null
    var fileSize: String? = null
    var bookingdatetime: String? = null
    var visittype: String? = null

    /*

    public static String getCreName() {
        return creName;
    }
*/
    fun setCreName(creName: String?) {
        this.creName = creName
    }

    constructor() {}
    protected constructor(`in`: Parcel) {
        alreadyservicedatId = `in`.readString()
        callCount = `in`.readString()
        dealerCode = `in`.readString()
        noResponseScheduledCallDate = `in`.readString()
        noServiceReason = `in`.readString()
        followUpDate = `in`.readString()
        followUpTime = `in`.readString()
        pickUpAddress = `in`.readString()
        isFollowupRequired = `in`.readString()
        customerName = `in`.readString()
        currentAddress = `in`.readString()
        appointmentType = `in`.readString()
        permanentAddress = `in`.readString()
        officeAddress = `in`.readString()
        customerEmail = `in`.readString()
        customerPhone = `in`.readString()
        doa = `in`.readString()
        dob = `in`.readString()
        agentName = `in`.readString()
        callType = `in`.readString()
        freeService = `in`.readString()
        lastCallDate = `in`.readString()
        nextCallDate = `in`.readString()
        notes = `in`.readString()
        pickUpRequired = `in`.readString()
        serviceScheduled = `in`.readString()
        serviceScheduledTime = `in`.readString()
        serviceScheduledDate = `in`.readString()
        status = `in`.readString()
        appointmentDate = `in`.readString()
        defectDetails = `in`.readString()
        labourDetails = `in`.readString()
        lastServiceDate = `in`.readString()
        milege = `in`.readString()
        model = `in`.readString()
        nextServiceDue = `in`.readString()
        saleDate = `in`.readString()
        serviceAdvisor = `in`.readString()
        serviceType = `in`.readString()
        technician = `in`.readString()
        vehicleNumber = `in`.readString()
        lastBatteryChangeDate = `in`.readString()
        lastDrumBrakeChange = `in`.readString()
        lastOilChange = `in`.readString()
        vasNotes = `in`.readString()
        filePath = `in`.readString()
        callDate = `in`.readString()
        callTime = `in`.readString()
        callDuration = `in`.readString()
        ringTime = `in`.readString()
        latitude = `in`.readString()
        longitude = `in`.readString()
        comments = `in`.readString()
        isCallMade = `in`.readString()
        mediaFile = `in`.readString()
        callTypePicId = `in`.readInt()
        vehicalRegNo = `in`.readString()
        totalVisitCount = `in`.readString()
        idealVisitCount = `in`.readString()
        visitSuccessRate = `in`.readString()
        lastVisitMileage = `in`.readString()
        lastVisitDate = `in`.readString()
        nextServiceDueDateForLastVisitDate = `in`.readString()
        nextServiceDueDateForMileage = `in`.readString()
        isFollowUpDone = `in`.readString()
        averageMileage = `in`.readString()
        noResponseOfCallMade = `in`.readString()
        /*lastServicedate=in.readString();*/lastServiceMileage = `in`.readString()
        lastServiceType = `in`.readString()
        lastVisitType = `in`.readString()
        averageRunning = `in`.readString()
        serviceNoShowPeriod = `in`.readString()
        serviceDueBasedonMileage = `in`.readString()
        serviceDueBasedonTenure = `in`.readString()
        nextServiceType = `in`.readString()
        forecastLogic = `in`.readString()
        variant = `in`.readString()
        dueDate = `in`.readString()
        district = `in`.readString()
        city = `in`.readString()
        bookingdatetime = `in`.readString()
        makeCallFrom = `in`.readString()
        roNumber = `in`.readString()
        roDate = `in`.readString()
        salutation = `in`.readString()
        customerCategory = `in`.readString()
        attendproperly = `in`.readString()
        workdoneproperly = `in`.readString()
        overallinfrastrucure = `in`.readString()
        workdonexplanation = `in`.readString()
        washingquality = `in`.readString()
        overallrating = `in`.readString()
        customersatisfication = `in`.readString()
        firebaseKey = `in`.readString()
        firebaseRef = `in`.readString()
        coverNoteDate = `in`.readString()
        nextRenewalDate = `in`.readString()
        coverNote = `in`.readString()
        insuranceCompany = `in`.readString()
        lastIDV = `in`.readString()
        ncBPercentage = `in`.readString()
        odPercentage = `in`.readString()
        odAmount = `in`.readString()
        premiumAmount = `in`.readString()
        serviceLocation = `in`.readString()
        co_Dealer_Outside = `in`.readString()
        mileageAtService = `in`.readString()
        mileage = `in`.readString()
        reason = `in`.readString()
        dealerName = `in`.readString()
        droppedcount = `in`.readString()
        noncontactautodialDate = `in`.readString()
        noncontactautodialTime = `in`.readString()
        reasonForHTML = `in`.readString()
        dateOfService = `in`.readString()
        insuredParty = `in`.readString()
        ncBAmount = `in`.readString()
        insuranceDueDate = `in`.readString()
        alternateMobileno = `in`.readString()
        alternateVehicleRegno = `in`.readString()
        uniqueDataCallInit = `in`.readString()
        booked = `in`.readString()
        received = `in`.readString()
        noShow = `in`.readString()
        totalAssignedCalls = `in`.readString()
        conversionRate = `in`.readString()
        pendingCalls = `in`.readString()
        uniqueidForCallSync = `in`.readInt()
        serviceBooked = `in`.readString()
        pickupStarted = `in`.readString()
        pickupPointReached = `in`.readString()
        vehiclePicked = `in`.readString()
        deliveredAtws = `in`.readString()
        trackStatus = `in`.readString()
        cancelStatus = `in`.readString()
        lastDate = `in`.readString()
        customerCRN = `in`.readString()
        fuelType = `in`.readString()
        loyaltyType = `in`.readString()
        anniversary = `in`.readString()
        vehicleColor = `in`.readString()
        pickupOrDrop = `in`.readString()
        dropStarted = `in`.readString()
        deliveredToCustomer = `in`.readString()
        interactionDate = `in`.readString()
        interactionTime = `in`.readString()
        userId = `in`.readString()
        vehicleId = `in`.readString()
        serviceBookedId = `in`.readString()
        vehicleRegNo = `in`.readString()
        custId = `in`.readString()
        nextServiceDate = `in`.readString()
        jobCardNumber = `in`.readString()
        ageOfVehicle = `in`.readInt()
        daysBetweenVisit = `in`.readInt()
        chassisNo = `in`.readString()
        afterServiceSatisfication = `in`.readString()
        recentServiceSatisfication = `in`.readString()
        afterServiceComments = `in`.readString()
        recentServiceComments = `in`.readString()
        billDate = `in`.readString()
        workshop = `in`.readString()
        assignedId = `in`.readString()
        creName = `in`.readString()
        lastPremium = `in`.readString()
        policyNo = `in`.readString()
        insuranceCompany = `in`.readString()
        renewalType = `in`.readString()
        renewalMode = `in`.readString()
        addOns = `in`.readString()
        idvPercentage = `in`.readString()
        premium = `in`.readString()
        appointmentTime = `in`.readString()
        paymentReference = `in`.readString()
        paymentType = `in`.readString()
    }

    fun getlastIDV(): String? {
        return lastIDV
    }

    fun setlastIDV(lastIDV: String?) {
        this.lastIDV = lastIDV
    }

    fun getinsuranceCompany(): String? {
        return insuranceCompany
    }

    fun setinsuranceCompany(insuranceCompany: String?) {
        this.insuranceCompany = insuranceCompany
    }

    fun getconversionRate(): String? {
        return conversionRate
    }

    fun setconversionRate(conversionRate: String?) {
        this.conversionRate = conversionRate
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(alreadyservicedatId)
        dest.writeString(bookingdatetime)
        dest.writeString(callCount)
        dest.writeString(dealerCode)
        dest.writeString(noResponseScheduledCallDate)
        dest.writeString(noServiceReason)
        dest.writeString(followUpDate)
        dest.writeString(followUpTime)
        dest.writeString(pickUpAddress)
        dest.writeString(isFollowupRequired)
        dest.writeString(customerName)
        dest.writeString(currentAddress)
        dest.writeString(appointmentType)
        dest.writeString(permanentAddress)
        dest.writeString(officeAddress)
        dest.writeString(customerEmail)
        dest.writeString(customerPhone)
        dest.writeString(doa)
        dest.writeString(dob)
        dest.writeString(agentName)
        dest.writeString(callType)
        dest.writeString(freeService)
        dest.writeString(lastCallDate)
        dest.writeString(nextCallDate)
        dest.writeString(notes)
        dest.writeString(pickUpRequired)
        dest.writeString(serviceScheduled)
        dest.writeString(serviceScheduledTime)
        dest.writeString(serviceScheduledDate)
        dest.writeString(status)
        dest.writeString(appointmentDate)
        dest.writeString(defectDetails)
        dest.writeString(labourDetails)
        dest.writeString(lastServiceDate)
        dest.writeString(milege)
        dest.writeString(model)
        dest.writeString(nextServiceDue)
        dest.writeString(saleDate)
        dest.writeString(serviceAdvisor)
        dest.writeString(serviceType)
        dest.writeString(technician)
        dest.writeString(vehicleNumber)
        dest.writeString(lastBatteryChangeDate)
        dest.writeString(lastDrumBrakeChange)
        dest.writeString(lastOilChange)
        dest.writeString(vasNotes)
        dest.writeString(filePath)
        dest.writeString(callDate)
        dest.writeString(callTime)
        dest.writeString(callDuration)
        dest.writeString(ringTime)
        dest.writeString(latitude)
        dest.writeString(longitude)
        dest.writeString(comments)
        dest.writeString(isCallMade)
        dest.writeString(mediaFile)
        dest.writeInt(callTypePicId)
        dest.writeString(vehicalRegNo)
        dest.writeString(totalVisitCount)
        dest.writeString(idealVisitCount)
        dest.writeString(visitSuccessRate)
        dest.writeString(lastVisitMileage)
        dest.writeString(lastVisitDate)
        dest.writeString(nextServiceDueDateForLastVisitDate)
        dest.writeString(nextServiceDueDateForMileage)
        dest.writeString(isFollowUpDone)
        dest.writeString(averageMileage)
        dest.writeString(noResponseOfCallMade)


        /*dest.writeString(lastServicedate);*/dest.writeString(lastServiceMileage)
        dest.writeString(lastServiceType)
        dest.writeString(lastVisitType)
        dest.writeString(averageRunning)
        dest.writeString(serviceNoShowPeriod)
        dest.writeString(serviceDueBasedonMileage)
        dest.writeString(serviceDueBasedonTenure)
        dest.writeString(nextServiceType)
        dest.writeString(forecastLogic)
        dest.writeString(variant)
        dest.writeString(dueDate)
        dest.writeString(district)
        dest.writeString(city)
        dest.writeString(makeCallFrom)
        dest.writeString(roNumber)
        dest.writeString(salutation)
        dest.writeString(customerCategory)
        dest.writeString(roDate)
        dest.writeString(attendproperly)
        dest.writeString(workdoneproperly)
        dest.writeString(workdonexplanation)
        dest.writeString(overallinfrastrucure)
        dest.writeString(washingquality)
        dest.writeString(overallrating)
        dest.writeString(customersatisfication)
        dest.writeString(firebaseKey)
        dest.writeString(firebaseRef)
        dest.writeString(coverNoteDate)
        dest.writeString(nextRenewalDate)
        dest.writeString(coverNote)
        dest.writeString(insuranceCompany)
        dest.writeString(lastIDV)
        dest.writeString(ncBPercentage)
        dest.writeString(odPercentage)
        dest.writeString(odAmount)
        dest.writeString(premiumAmount)
        dest.writeString(serviceLocation)
        dest.writeString(co_Dealer_Outside)
        dest.writeString(mileageAtService)
        dest.writeString(mileage)
        dest.writeString(reason)
        dest.writeString(dealerName)
        dest.writeString(droppedcount)
        dest.writeString(noncontactautodialDate)
        dest.writeString(noncontactautodialTime)
        dest.writeString(reasonForHTML)
        dest.writeString(dateOfService)
        dest.writeString(insuredParty)
        dest.writeString(ncBAmount)
        dest.writeString(insuranceDueDate)
        dest.writeString(alternateMobileno)
        dest.writeString(alternateVehicleRegno)
        dest.writeString(uniqueDataCallInit)
        dest.writeString(booked)
        dest.writeString(received)
        dest.writeString(noShow)
        dest.writeString(totalAssignedCalls)
        dest.writeString(conversionRate)
        dest.writeString(pendingCalls)
        dest.writeInt(uniqueidForCallSync)
        dest.writeString(serviceBooked)
        dest.writeString(pickupStarted)
        dest.writeString(pickupPointReached)
        dest.writeString(vehiclePicked)
        dest.writeString(deliveredAtws)
        dest.writeString(trackStatus)
        dest.writeString(cancelStatus)
        dest.writeString(lastDate)
        dest.writeString(customerCRN)
        dest.writeString(fuelType)
        dest.writeString(loyaltyType)
        dest.writeString(anniversary)
        dest.writeString(vehicleColor)
        dest.writeString(pickupOrDrop)
        dest.writeString(dropStarted)
        dest.writeString(deliveredToCustomer)
        dest.writeString(interactionDate)
        dest.writeString(interactionTime)
        dest.writeString(userId)
        dest.writeString(vehicleId)
        dest.writeString(serviceBookedId)
        dest.writeString(vehicleRegNo)
        dest.writeString(custId)
        dest.writeString(nextServiceDate)
        dest.writeString(jobCardNumber)
        dest.writeInt(ageOfVehicle)
        dest.writeInt(daysBetweenVisit)
        dest.writeString(chassisNo)
        dest.writeString(afterServiceSatisfication)
        dest.writeString(recentServiceSatisfication)
        dest.writeString(afterServiceComments)
        dest.writeString(recentServiceComments)
        dest.writeString(billDate)
        dest.writeString(workshop)
        dest.writeString(assignedId)
        dest.writeString(creName)
        dest.writeString(lastPremium)
        dest.writeString(lastIDV)
        dest.writeString(insuranceCompany)
        dest.writeString(policyNo)
        dest.writeString(renewalType)
        dest.writeString(renewalMode)
        dest.writeString(addOns)
        dest.writeString(idvPercentage)
        dest.writeString(premium)
        dest.writeString(appointmentTime)
        dest.writeString(paymentReference)
        dest.writeString(paymentType)
    }

}